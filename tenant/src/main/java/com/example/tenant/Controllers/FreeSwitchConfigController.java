package com.example.tenant.Controllers;

import com.example.tenant.Entities.*;
import com.example.tenant.Entities.UsersConfig.CallGroup;
import com.example.tenant.Entities.UsersConfig.CallGroupStrategy;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Services.*;
import com.example.tenant.Services.UsersConfig.CallGroupService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/freeswitch")
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;
    @Autowired
    private TrunkService trunkService;
    @Autowired
    private DidNumberService didNumberService;
    @Autowired
    private TrunkPoolService trunkPoolService;
    @Autowired
    private CallGroupService callGroupService;

    @Autowired
    private CallLimitService callLimitService;

    @Autowired
    private DialPlanRestrictionService restrictionService;

    @Autowired
    private TimeRestrictionService timeRestrictionService;



    @RequestMapping(value = "/config", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> generateXml(@RequestParam Map<String, String> allParams) {

        String section = allParams.get("section");
        String tagName = allParams.get("tag_name");
        String keyValue = allParams.get("key_value");

        // Gestion des valeurs par défaut pour dialplan et directory
        if ("dialplan".equals(section)) {
            if (!StringUtils.hasText(tagName)) {
                tagName = "context";
            }
            if (!StringUtils.hasText(keyValue)) {
                keyValue = allParams.getOrDefault("Caller-Context", "");
            }
        }

        if ("directory".equals(section)) {
            if (!StringUtils.hasText(tagName)) {
                tagName = "domain";
            }
            if (!StringUtils.hasText(keyValue)) {
                keyValue = allParams.getOrDefault("domain", "");
            }
        }

        // On récupère le domaine pertinent selon la section
        String domainToCheck = null;
        if ("directory".equals(section) && "domain".equals(tagName)) {
            domainToCheck = keyValue;
        } else if ("dialplan".equals(section) && "context".equals(tagName)) {
            domainToCheck = keyValue;
        }

        // Filtrer uniquement les domaines présents et actifs en base
        if (domainToCheck != null && !domainToCheck.isEmpty()) {
            Optional<Tenant> tenantOpt = tenantService.getByDomain(domainToCheck);
            if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
                // Domaine non trouvé ou inactif : on ne traite pas et on ne log pas en détail
                return notFoundXml();
            }
        }

        // Maintenant on peut traiter selon la section

        if ("dialplan".equals(section) && "context".equals(tagName)) {
            String callerDestNumber = allParams.get("Caller-Destination-Number");
            return generateDialplanXml(keyValue, callerDestNumber);
        }

        if ("directory".equals(section) && "domain".equals(tagName)) {
            String user = allParams.get("user");
            return generateDirectoryXml(keyValue, user);
        }

        if ("configuration".equals(section) && "configuration".equals(tagName) && "voicemail.conf".equals(keyValue)) {
            String profileName = allParams.get("profile");
            if (!StringUtils.hasText(profileName)) {
                profileName = keyValue;
            }
            return generateVoicemailConfXml(profileName);
        }

        // Pour toutes les autres requêtes inconnues ou non gérées
        return notFoundXml();
    }
//original
/*
    private ResponseEntity<String> generateDialplanXml(String contextName, String destNumber) {

        if (!StringUtils.hasText(contextName)) {
            System.out.println("ERREUR: contextName est vide");
            return notFoundXml();
        }

        Optional<Tenant> tenantOpt = tenantService.getByDomain(contextName);
        if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
            System.out.println("Tenant non trouvé ou inactif pour: " + contextName);
            return notFoundXml();
        }

        Tenant tenant = tenantOpt.get();
        System.out.println("Tenant trouvé: " + tenant.getDomainName());

        // Récupérer les trunks actifs
        List<Trunk> trunks = trunkPoolService.getActiveTrunksByTenant(tenant.getId());
        String defaultTrunkName = trunks.isEmpty() ? "" : trunks.get(0).getName();

        // Récupérer les DIDs actifs
        List<DidNumber> dids = didNumberService.getActiveDidsByTenant(tenant.getId());

        // Récupérer les pools actifs
        List<TrunkPool> pools = trunkPoolService.getActivePoolsByTenant(tenant.getId());
        // Groupes d'appel
        List<CallGroup> groups = callGroupService.getAllCallGroupsWithMembers()
                .stream().filter(CallGroup::isActive).toList();


        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<document type=\"freeswitch/xml\">\n");
        xml.append("  <section name=\"dialplan\">\n");
        xml.append("    <context name=\"").append(contextName).append("\">\n");

        // ---------- Groupes d’appel ----------
        for (CallGroup group : groups) {
            if (!StringUtils.hasText(group.getExtension())) continue;
            if (group.getMembers() == null || group.getMembers().isEmpty()) continue;

            String endpoints = group.getMembers().stream()
                    .filter(Objects::nonNull)
                    .map(m -> "user/" + m.getExtension() + "@" + contextName)
                    .collect(Collectors.joining(","));

            String chanVars = "{ignore_early_media=true,ringback=${us-ring},originate_timeout=25,leg_timeout=25,hangup_after_bridge=true,continue_on_fail=true}";

            xml.append("      <extension name=\"callgroup_").append(group.getExtension()).append("\">\n");
            xml.append("        <condition field=\"destination_number\" expression=\"^").append(group.getExtension()).append("$\">\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");
            xml.append("          <action application=\"bridge\" data=\"").append(chanVars).append(endpoints).append("\"/>\n");
            xml.append("        </condition>\n");
            xml.append("      </extension>\n");
        }
        // ====== Gestion des DIDs individuels ======
        for (DidNumber did : dids) {
            xml.append("      <extension name=\"did_").append(did.getNumber()).append("\">\n");
            xml.append("        <condition field=\"destination_number\" expression=\"^").append(did.getNumber()).append("$\">\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");

            switch (did.getDestinationType()) {
                case USER:
                    xml.append("          <action application=\"bridge\" data=\"user/")
                            .append(did.getDestinationValue())
                            .append("@").append(contextName).append("\"/>\n");
                    break;
                case IVR:
                case QUEUE:
                    xml.append("          <action application=\"transfer\" data=\"")
                            .append(did.getDestinationValue())
                            .append("@").append(contextName).append("\"/>\n");
                    break;
                default:
                    if (!defaultTrunkName.isEmpty()) {
                        xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                                .append(defaultTrunkName).append("/").append(did.getNumber()).append("\"/>\n");
                    }
                    break;
            }
            xml.append("        </condition>\n");
            xml.append("      </extension>\n");
        }

        // ====== Gestion des DIDs depuis les pools ======
        for (TrunkPool pool : pools) {
            List<Trunk> poolTrunks = pool.getTrunks().stream()
                    .filter(Trunk::isActive)
                    .toList();

            for (int num = pool.getStartNumber(); num <= pool.getEndNumber(); num++) {
                String didNumber = pool.getCountryCode() + pool.getAreaCode() + pool.getLocalCode()
                        + String.format("%04d", num); // padding à 4 chiffres

                xml.append("      <extension name=\"pool_did_").append(didNumber).append("\">\n");
                xml.append("        <condition field=\"destination_number\" expression=\"^").append(didNumber).append("$\">\n");
                xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
                xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");

                if (!poolTrunks.isEmpty()) {
                    // On prend le premier trunk actif du pool
                    xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                            .append(poolTrunks.get(0).getName()).append("/").append(didNumber).append("\"/>\n");
                } else if (!defaultTrunkName.isEmpty()) {
                    xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                            .append(defaultTrunkName).append("/").append(didNumber).append("\"/>\n");
                }

                xml.append("        </condition>\n");
                xml.append("      </extension>\n");
            }
        }

        // ====== Appels internes ======
        xml.append("      <extension name=\"local_calls\">\n");
        xml.append("        <condition field=\"destination_number\" expression=\"^(\\d{4})$\">\n");
        xml.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
        xml.append("          <action application=\"bridge\" data=\"user/$1@").append(contextName).append("\"/>\n");
        xml.append("          <action application=\"voicemail\" data=\"").append(contextName).append(" $1\"/>\n");
        xml.append("        </condition>\n");
        xml.append("      </extension>\n");

        // ====== Appels sortants ======
        xml.append("      <extension name=\"outbound_calls\">\n");
        xml.append("        <condition field=\"destination_number\" expression=\"^\\d+$\">\n");
        xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
        xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");
        xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                .append(defaultTrunkName).append("/$1\"/>\n");
        xml.append("        </condition>\n");
        xml.append("      </extension>\n");

        xml.append("    </context>\n");
        xml.append("  </section>\n");
        xml.append("</document>");

        return ResponseEntity.ok(xml.toString());
    }
*/

    private ResponseEntity<String> generateDialplanXml(String contextName, String destNumber) {

        if (!StringUtils.hasText(contextName)) {
            System.out.println("ERREUR: contextName est vide");
            return notFoundXml();
        }

        Optional<Tenant> tenantOpt = tenantService.getByDomain(contextName);
        if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
            System.out.println("Tenant non trouvé ou inactif pour: " + contextName);
            return notFoundXml();
        }

        Tenant tenant = tenantOpt.get();
        System.out.println("Tenant trouvé: " + tenant.getDomainName());

        // Récupérer les trunks actifs
        List<Trunk> trunks = trunkPoolService.getActiveTrunksByTenant(tenant.getId());
        String defaultTrunkName = trunks.isEmpty() ? "" : trunks.get(0).getName();

        // Récupérer les DIDs actifs
        List<DidNumber> dids = didNumberService.getActiveDidsByTenant(tenant.getId());

        // Récupérer les pools actifs
        List<TrunkPool> pools = trunkPoolService.getActivePoolsByTenant(tenant.getId());

        // Groupes d'appel
        List<CallGroup> groups = callGroupService.getAllCallGroupsWithMembers()
                .stream().filter(CallGroup::isActive).toList();

        List<SipProfile> profiles = sipUserService.getActiveProfilesByTenant(tenant.getId());

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<document type=\"freeswitch/xml\">\n");
        xml.append("  <section name=\"dialplan\">\n");
        xml.append("    <context name=\"").append(contextName).append("\">\n");

        // ------------------ Gestion des restrictions SIP ------------------
        for (SipProfile profile : profiles) {

            CallLimit limit = callLimitService.getBySipProfile(profile.getId());
            List<DialPlanRestriction> restrictions = restrictionService.getBySipProfile(profile.getId());
            List<TimeRestriction> timeRestrictions = timeRestrictionService.getBySipProfile(profile.getId());

            if (!restrictions.isEmpty() || limit != null || !timeRestrictions.isEmpty()) {

                xml.append("      <extension name=\"ext_").append(profile.getExtension()).append("\">\n");
                xml.append("        <action application=\"log\" data=\"DEBUG: Processing restrictions for ")
                        .append(profile.getExtension()).append("\"/>\n");

                if (limit != null) {
                    xml.append("        <action application=\"limit\" data=\"hash ")
                            .append(contextName).append(" ").append(profile.getExtension()).append(" ")
                            .append(limit.getMaxConcurrentCalls()).append(" !USER_BUSY\"/>\n");
                }

                for (TimeRestriction t : timeRestrictions) {
                    if (t.isAllow()) {
                        xml.append("        <condition field=\"time-of-day\" expression=\"")
                                .append(t.getStartTime()).append("-").append(t.getEndTime()).append("\"/>\n");
                        xml.append("        <action application=\"log\" data=\"DEBUG: Time restriction applied for ")
                                .append(profile.getExtension()).append("\"/>\n");
                    }
                }

                for (DidNumber did : dids) {
                    Trunk trunk = did.getTrunk();
                    TrunkPool pool = trunk.getTrunkPool();

                    // DID complet
                    String fullDidNumber = "" + pool.getCountryCode() + pool.getAreaCode() + pool.getLocalCode() + did.getNumber();

                    boolean blocked = false;
                    for (DialPlanRestriction r : restrictions) {
                        String prefix = r.getRegex(); // ex: "10" ou "11"

                        // Ici on ne compare que le DID réel (dernier segment)
                        if (!r.isAllowed() && did.getNumber().startsWith(prefix)) {
                            blocked = true;
                            break;
                        }
                    }

                    if (blocked) {
                        xml.append("        <condition field=\"destination_number\" expression=\"^").append(fullDidNumber).append("$\"/>\n");
                        xml.append("          <action application=\"hangup\" data=\"CALL_REJECTED\"/>\n");
                        xml.append("          <action application=\"log\" data=\"DEBUG: DID ").append(fullDidNumber).append(" bloqué pour profil ")
                                .append(profile.getExtension()).append("\"/>\n");
                    } else {
                        xml.append("        <condition field=\"destination_number\" expression=\"^").append(fullDidNumber).append("$\"/>\n");
                        switch (did.getDestinationType()) {
                            case USER:
                                xml.append("          <action application=\"bridge\" data=\"user/")
                                        .append(did.getDestinationValue())
                                        .append("@").append(contextName).append("\"/>\n");
                                break;
                            case IVR:
                            case QUEUE:
                                xml.append("          <action application=\"transfer\" data=\"")
                                        .append(did.getDestinationValue())
                                        .append("@").append(contextName).append("\"/>\n");
                                break;
                            default:
                                if (!defaultTrunkName.isEmpty()) {
                                    xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                                            .append(defaultTrunkName).append("/").append(fullDidNumber).append("\"/>\n");
                                }
                                break;
                        }
                        xml.append("          <action application=\"log\" data=\"DEBUG: DID ").append(fullDidNumber).append(" autorisé pour profil ")
                                .append(profile.getExtension()).append("\"/>\n");
                    }
                }

                xml.append("      </extension>\n");
            }
        }

        // ---------- Groupes d’appel ----------
        for (CallGroup group : groups) {
            if (!StringUtils.hasText(group.getExtension())) continue;
            if (group.getMembers() == null || group.getMembers().isEmpty()) continue;

            String endpoints = group.getMembers().stream()
                    .filter(Objects::nonNull)
                    .map(m -> "user/" + m.getExtension() + "@" + contextName)
                    .collect(Collectors.joining(","));

            String chanVars = "{ignore_early_media=true,ringback=${us-ring},originate_timeout=25,leg_timeout=25,hangup_after_bridge=true,continue_on_fail=true}";

            xml.append("      <extension name=\"callgroup_").append(group.getExtension()).append("\">\n");
            xml.append("        <condition field=\"destination_number\" expression=\"^").append(group.getExtension()).append("$\">\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");
            xml.append("          <action application=\"bridge\" data=\"").append(chanVars).append(endpoints).append("\"/>\n");
            xml.append("        </condition>\n");
            xml.append("      </extension>\n");
        }

        // ====== Gestion des DIDs individuels ======
        for (DidNumber did : dids) {
            xml.append("      <extension name=\"did_").append(did.getNumber()).append("\">\n");
            xml.append("        <condition field=\"destination_number\" expression=\"^").append(did.getNumber()).append("$\">\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
            xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");

            switch (did.getDestinationType()) {
                case USER:
                    xml.append("          <action application=\"bridge\" data=\"user/")
                            .append(did.getDestinationValue())
                            .append("@").append(contextName).append("\"/>\n");
                    break;
                case IVR:
                case QUEUE:
                    xml.append("          <action application=\"transfer\" data=\"")
                            .append(did.getDestinationValue())
                            .append("@").append(contextName).append("\"/>\n");
                    break;
                default:
                    if (!defaultTrunkName.isEmpty()) {
                        xml.append("          <action application=\"bridge\" data=\"sofia/gateway/")
                                .append(defaultTrunkName).append("/").append(did.getNumber()).append("\"/>\n");
                    }
                    break;
            }
            xml.append("        </condition>\n");
            xml.append("      </extension>\n");
        }

        // ====== Appels internes ======
        xml.append("      <extension name=\"local_calls\">\n");
        xml.append("        <condition field=\"destination_number\" expression=\"^(\\d{4})$\">\n");
        xml.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
        xml.append("          <action application=\"bridge\" data=\"user/$1@").append(contextName).append("\"/>\n");
        xml.append("          <action application=\"voicemail\" data=\"").append(contextName).append(" $1\"/>\n");
        xml.append("        </condition>\n");
        xml.append("      </extension>\n");

        // ====== Appels sortants ======
        xml.append("      <extension name=\"outbound_calls\">\n");
        xml.append("        <condition field=\"destination_number\" expression=\"^\\d+$\">\n");
        xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
        xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");
        xml.append("          <action application=\"bridge\" data=\"sofia/gateway/").append(defaultTrunkName).append("/$1\"/>\n");
        xml.append("        </condition>\n");
        xml.append("      </extension>\n");

        xml.append("    </context>\n");
        xml.append("  </section>\n");
        xml.append("</document>");

        return ResponseEntity.ok(xml.toString());
    }




    private ResponseEntity<String> generateDirectoryXml(String domain, String user) {

        if (!StringUtils.hasText(domain) || !StringUtils.hasText(user)) {
            System.out.println("ERREUR: domain ou user vide - domain: " + domain + ", user: " + user);
            return notFoundXml();
        }

        Optional<SipProfile> sipProfileOpt = sipUserService.findByDomainAndExtension(domain, user);

        if (sipProfileOpt.isEmpty() || !sipProfileOpt.get().isActive()) {
            System.out.println("Profil SIP non trouvé pour " + user + "@" + domain);
            return notFoundXml();
        }

        SipProfile profile = sipProfileOpt.get();
        System.out.println("Profil SIP trouvé: " + profile.getExtension() + "@" + domain);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<document type=\"freeswitch/xml\">\n");
        xml.append("  <section name=\"directory\">\n");
        xml.append("    <domain name=\"").append(domain).append("\">\n");
        xml.append("      <params>\n");
        xml.append("        <param name=\"dial-string\" value=\"{presence_id=${dialed_user}@${dialed_domain}}${sofia_contact(${dialed_user}@${dialed_domain})}\"/>\n");
        xml.append("      </params>\n");
        xml.append("      <groups>\n");
        xml.append("        <group name=\"default\">\n");
        xml.append("          <users>\n");
        xml.append("            <user id=\"").append(profile.getExtension()).append("\">\n");
        xml.append("              <params>\n");
        xml.append("                <param name=\"password\" value=\"").append(profile.getPassword()).append("\"/>\n");
        xml.append("              </params>\n");
        xml.append("              <variables>\n");
        xml.append("                <variable name=\"user_context\" value=\"").append(profile.getDomainName()).append("\"/>\n");
        xml.append("              </variables>\n");
        xml.append("            </user>\n");
        xml.append("          </users>\n");
        xml.append("        </group>\n");
        xml.append("      </groups>\n");
        xml.append("    </domain>\n");
        xml.append("  </section>\n");
        xml.append("</document>");

        return ResponseEntity.ok(xml.toString());
    }


    private ResponseEntity<String> generateVoicemailConfXml(String profileName) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<document type=\"freeswitch/xml\">\n");
        xml.append("  <section name=\"configuration\">\n");
        xml.append("    <configuration name=\"voicemail.conf\" description=\"Voicemail Config\">\n");
        xml.append("      <profiles>\n");
        xml.append("        <profile name=\"").append(profileName).append("\">\n");
        xml.append("          <param name=\"odbc-dsn\" value=\"\"/>\n");
        xml.append("          <param name=\"dbname\" value=\"voicemail\"/>\n");
        xml.append("          <param name=\"record-template\" value=\"$${recordings_dir}/vm-$${domain}-$${user}.wav\"/>\n");
        xml.append("          <param name=\"storage-dir\" value=\"$${base_dir}/storage\"/>\n");
        xml.append("          <param name=\"terminate-key\" value=\"#\"/>\n");
        xml.append("        </profile>\n");
        xml.append("      </profiles>\n");
        xml.append("    </configuration>\n");
        xml.append("  </section>\n");
        xml.append("</document>");

        return ResponseEntity.ok(xml.toString());
    }


    private ResponseEntity<String> notFoundXml() {
        return ResponseEntity.status(404).body(
                "<document type=\"freeswitch/xml\">" +
                        "<section name=\"result\">" +
                        "<result status=\"not found\"/>" +
                        "</section>" +
                        "</document>"
        );
    }
}