package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UsersConfig.CallGroup;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.CallGroupRepository;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.loadbalancer.config.LoadBalancerCacheAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/freeswitch/config")
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private DidNumberService didNumberService;
    @Autowired
    private CallGroupRepository callGroupRepository;
    @Autowired
    private LoadBalancerCacheAutoConfiguration logger;


    /*@PostMapping(value = "/config", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getFreeSwitchConfig(
            @RequestParam String section,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String user,
            @RequestParam(required = false, name = "context") String contextName,
            @RequestParam(required = false, name = "destination_number") String destNumber,
            @RequestParam(required = false, name = "key_name") String keyName,
            @RequestParam(required = false, name = "profile") String profile
    ) {
        if ("directory".equals(section)) {
            if (domain != null && user != null) {
                return generateDirectoryXml(domain, user);
            }
        } else if ("dialplan".equals(section)) {
            if (contextName != null && destNumber != null) {
                return generateDialplanXml(contextName, destNumber);
            }
        } else if ("configuration".equals(section)) {
            return generateConfigurationXml(keyName, profile);
        }

        return notFoundXml();
    }*/

    @GetMapping("/dialplan")
   private ResponseEntity<String> generateDialplanXml(String domainName, String destNumber) {
        System.out.println("generateDialplanXml appelé avec domainName=" + domainName + " et destNumber=" + destNumber);

        Optional<Tenant> tenantOpt = tenantService.getByDomain(domainName);

        if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
            System.out.println("Tenant non trouvé ou inactif pour le domaine : " + domainName);
            return notFoundXml();
        }

        Tenant tenant = tenantOpt.get();
        System.out.println("Tenant trouvé : " + tenant.getDomainName() + " (ID=" + tenant.getId() + ")");

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<document type=\"freeswitch/xml\">\n");
        xmlBuilder.append("  <section name=\"dialplan\" description=\"Dynamic Dialplan\">\n");
        xmlBuilder.append("    <context name=\"").append(domainName).append("\">\n");


       // Ajouter les variables ici
       xmlBuilder.append("      <variables>\n");
       xmlBuilder.append("          <variable name=\"user_context\" value=\"" + domainName + "\"/>\n");
       xmlBuilder.append("      </variables>\n");



        // Règle pour check voicemail (123)
        xmlBuilder.append("      <extension name=\"check_voicemail\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^123$\">\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"check ").append(domainName).append("\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");

        // 1. Appels internes tenant (extensions 4 chiffres)
        String extensionPattern = "^(\\d{4})$";
        xmlBuilder.append("      <extension name=\"local_call_within_tenant\">\n");
       xmlBuilder.append("        <variable name=\"user_context\" value=\"").append(domainName).append("\"/>\n");
       xmlBuilder.append("        <condition field=\"destination_number\" expression=\"").append(extensionPattern).append("\">\n");
        xmlBuilder.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
        xmlBuilder.append("          <action application=\"bridge\" data=\"user/$1@").append(domainName).append("\"/>\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"").append(domainName).append(" $1\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");

        // 2. Gestion du DID entrant
        System.out.println("Recherche DID pour numéro : " + destNumber);
        Optional<DidNumber> didOpt = didNumberService.findActiveDid(destNumber);
        System.out.println("DID trouvé ? " + didOpt.isPresent());

        if (didOpt.isPresent()) {
            DidNumber did = didOpt.get();
            System.out.println("DID extension : " + did.getExtension());
            System.out.println("DID SIP Profile ID : " + did.getSipProfile().getId());

            Optional<SipProfile> sipOpt = sipUserService.findById(did.getSipProfile().getId());
            System.out.println("SIP Profile trouvé ? " + sipOpt.isPresent());

            if (sipOpt.isPresent()) {
                SipProfile sipProfile = sipOpt.get();

                System.out.println("Tenant du SIP Profile : " + sipProfile.getTenantId());
                System.out.println("Tenant actuel : " + tenant.getId());

                // Vérifie que le SIP profile appartient au tenant actuel
                if (sipProfile.getTenantId().equals(tenant.getId())) {
                    // Récupération de l'extension du SIP Profile
                    String sipExtension = sipProfile.getExtension();

                    // Extension DID
                    xmlBuilder.append("      <extension name=\"incoming_did\">\n");
                    xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^").append(destNumber).append("$\">\n");
                    xmlBuilder.append("          <action application=\"bridge\" data=\"user/").append(sipExtension).append("@").append(domainName).append("\"/>\n");
                    xmlBuilder.append("          <action application=\"voicemail\" data=\"").append(domainName).append(" ").append(sipExtension).append("\"/>\n");
                    xmlBuilder.append("        </condition>\n");
                    xmlBuilder.append("      </extension>\n");

                    // --- NOUVEAU : Ajouter les CallGroups de ce SIP Profile ---
                    List<CallGroup> callGroups = callGroupRepository.findByMembers_Id(sipProfile.getId());

                    for (CallGroup group : callGroups) {
                        String groupExtension = group.getExtension();
                        if (groupExtension != null && !groupExtension.isEmpty()) {
                            xmlBuilder.append("      <extension name=\"callgroup_").append(group.getId()).append("\">\n");
                            xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^").append(groupExtension).append("$\">\n");
                            xmlBuilder.append("          <action application=\"bridge\" data=\"user/").append(sipExtension).append("@").append(domainName).append("\"/>\n");
                            xmlBuilder.append("          <action application=\"voicemail\" data=\"").append(domainName).append(" ").append(sipExtension).append("\"/>\n");

                            xmlBuilder.append("        </condition>\n");
                            xmlBuilder.append("      </extension>\n");
                        }
                    }
                } else {
                    System.out.println("Le SIP Profile ne correspond pas au tenant courant.");
                }
            }
        }

        // 3. Ajouter d'autres règles si besoin...

        xmlBuilder.append("    </context>\n");
        xmlBuilder.append("  </section>\n");
        xmlBuilder.append("</document>");

        return ResponseEntity.ok(xmlBuilder.toString());
    }


   @GetMapping("/directory")
    public ResponseEntity<String> generateDirectoryXml(
            @RequestParam("domain") String domain,
            @RequestParam("user") String user) {

        Optional<SipProfile> sipProfileOpt = sipUserService.findByDomainAndExtension(domain, user);
        if (sipProfileOpt.isEmpty() || !sipProfileOpt.get().isActive()) {
            return notFoundXml();
        }
        String cleanDomain = domain.split(",")[0];
        String cleanUser = user.split(",")[0];
        SipProfile profile = sipProfileOpt.get();

        // 3. Génère le XML attendu par FreeSWITCH avec le vrai domaine
        String xml =
                "<document type=\"freeswitch/xml\">\n" +
                        "  <section name=\"directory\">\n" +
                        "    <domain name=\"" + cleanDomain + "\">\n" +
                        "      <user id=\"" + profile.getExtension() + "\">\n" +
                        "        <params>\n" +
                        "          <param name=\"password\" value=\"" + (profile.getPassword() != null ? profile.getPassword() : "") + "\"/>\n" +
                        "          <param name=\"vm-password\" value=\"" + (profile.getPassword() != null ? profile.getPassword() : "") + "\"/>\n" +
                        "        </params>\n" +
                        "        <variables>\n" +
                        "          <variable name=\"toll_allow\" value=\"" + "domestic,international,local" + "\"/>\n" +
                        "          <variable name=\"accountcode\" value=\"" + (profile.getExtension() != null ? profile.getExtension() : "") + "\"/>\n" +
                        "          <variable name=\"user_context\" value=\"" + "default" + "\"/>\n" +
                        "          <variable name=\"effective_caller_id_name\" value=\"" + (profile.getDomainName() != null ? profile.getDomainName() : profile.getExtension()) + "\"/>\n" +
                        "          <variable name=\"effective_caller_id_number\" value=\"" + (profile.getExtension() != null ? profile.getExtension(): "") + "\"/>\n" +
                        "          <variable name=\"outbound_caller_id_name\" value=\"" + (profile.getUsername() != null ? profile.getUsername() : "") + "\"/>\n" +
                        "          <variable name=\"outbound_caller_id_number\" value=\"" + (profile.getExtension() != null ? profile.getExtension() : "") + "\"/>\n" +
                        "        </variables>\n" +
                        "      </user>\n" +
                        "    </domain>\n" +
                        "  </section>\n" +
                "</document>";

        System.out.println("XML généré :\n{}"+ xml);
        return ResponseEntity.ok(xml);
    }

    private ResponseEntity<String> generateConfigurationXml(String keyName, String profile) {
        if ("sofia.conf".equalsIgnoreCase(keyName) && "internal".equalsIgnoreCase(profile)) {
            String xml =
                    "<document type=\"freeswitch/xml\">\n" +
                            "  <section name=\"configuration\">\n" +
                            "    <configuration name=\"sofia.conf\" description=\"SIP\">\n" +
                            "      <profiles>\n" +
                            "        <profile name=\"internal\">\n" +
                            "          <gateways/>\n" +
                            "          <domains>\n" +
                            "            <domain name=\"all\" parse=\"true\"/>\n" +
                            "          </domains>\n" +
                            "          <settings>\n" +
                            "            <param name=\"context\" value=\"default\"/>\n" +
                            "            <param name=\"sip-port\" value=\"5060\"/>\n" +
                            "            <param name=\"rtp-ip\" value=\"auto\"/>\n" +
                            "            <param name=\"sip-ip\" value=\"auto\"/>\n" +
                            "            <param name=\"ext-rtp-ip\" value=\"auto-nat\"/>\n" +
                            "            <param name=\"ext-sip-ip\" value=\"auto-nat\"/>\n" +
                            "            <param name=\"dialplan\" value=\"XML\"/>\n" +
                            "            <param name=\"inbound-codec-prefs\" value=\"PCMU,PCMA,OPUS\"/>\n" +
                            "            <param name=\"outbound-codec-prefs\" value=\"PCMU,PCMA,OPUS\"/>\n" +
                            "            <param name=\"auth-calls\" value=\"true\"/>\n" +
                            "            <param name=\"rtp-timeout-sec\" value=\"300\"/>\n" +
                            "            <param name=\"rtp-hold-timeout-sec\" value=\"1800\"/>\n" +
                            "            <param name=\"manage-presence\" value=\"true\"/>\n" +
                            "            <param name=\"record-path\" value=\"/var/lib/freeswitch/recordings\"/>\n" +
                            "            <param name=\"record-template\" value=\"${caller_id_number}_${uuid}.wav\"/>\n" +
                            "            <param name=\"watchdog-enabled\" value=\"false\"/>\n" +
                            "          </settings>\n" +
                            "        </profile>\n" +
                            "      </profiles>\n" +
                            "    </configuration>\n" +
                            "  </section>\n" +
                            "</document>";

            return ResponseEntity.ok(xml);
        }

        return notFoundXml();
    }



    private ResponseEntity<String> notFoundXml() {
        String notFound = "<document type=\"freeswitch/xml\"><section name=\"result\"><result status=\"not found\" /></section></document>";
        return ResponseEntity.ok(notFound);
    }

}