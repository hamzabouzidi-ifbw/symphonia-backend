package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.TrunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/freeswitch")
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;
    @Autowired
    private TrunkService trunkService;


    @RequestMapping(value = "/config", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> generateXml(@RequestParam Map<String, String> allParams) {

        // Logger tous les paramètres reçus
        System.out.println("=== PARAMETRES REÇUS ===");
        allParams.forEach((key, value) -> System.out.println(key + " = " + value));

        String section = allParams.get("section");
        String tagName = allParams.get("tag_name");
        String keyValue = allParams.get("key_value");

        // Fallbacks pour DIALPLAN et DIRECTORY
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

        // 📞 DIALPLAN
        if ("dialplan".equals(section) && "context".equals(tagName)) {
            String callerDestNumber = allParams.get("Caller-Destination-Number");
            System.out.println("Requête DIALPLAN pour contexte: " + keyValue + ", destination: " + callerDestNumber);
            return generateDialplanXml(keyValue, callerDestNumber);
        }

        // 👤 DIRECTORY
        if ("directory".equals(section) && "domain".equals(tagName)) {
            String user = allParams.get("user");
            System.out.println("Requête DIRECTORY pour domaine: " + keyValue + ", user: " + user);
            return generateDirectoryXml(keyValue, user);
        }

        // ⚙️ CONFIGURATION: voicemail.conf
        if ("configuration".equals(section) && "configuration".equals(tagName) && StringUtils.hasText(keyValue) && "voicemail.conf".equals(keyValue)) {
            String profileName = allParams.get("profile");
            if (!StringUtils.hasText(profileName)) {
                profileName = allParams.get("key_value"); // fallback
            }
            System.out.println("Requête CONFIGURATION pour voicemail.conf, profil=" + profileName);
            return generateVoicemailConfXml(profileName);
        }

        System.out.println("Section non gérée: " + section + ", tagName: " + tagName + ", keyValue: " + keyValue);
        return notFoundXml();
    }


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

    // Récupérer la liste des trunks actifs du tenant
    List<Trunk> trunks = trunkService.getTrunksByTenant(tenant.getId())
            .stream()
            .filter(Trunk::isActive)
            .toList();

    // Choisir un trunk (par exemple le premier actif)
    String trunkName = trunks.isEmpty() ? "" : trunks.get(0).getName();

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
    xml.append("<document type=\"freeswitch/xml\">\n");
    xml.append("  <section name=\"dialplan\">\n");
    xml.append("    <context name=\"").append(contextName).append("\">\n");

    // Extension appels internes
    xml.append("      <extension name=\"local_calls\">\n");
    xml.append("        <condition field=\"destination_number\" expression=\"^(\\d{4})$\">\n");
    xml.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
    xml.append("          <action application=\"bridge\" data=\"user/$1@").append(contextName).append("\"/>\n");
    xml.append("          <action application=\"voicemail\" data=\"").append(contextName).append(" $1\"/>\n");
    xml.append("        </condition>\n");
    xml.append("      </extension>\n");

    // Nouvelle extension appels sortants via trunk
    // Supposons que les appels externes commencent par numéro
    xml.append("      <extension name=\"outbound_calls\">\n");
    xml.append("        <condition field=\"destination_number\" expression=\"^\\d+$\">\n");
    xml.append("          <action application=\"set\" data=\"effective_caller_id_name=${caller_id_name}\"/>\n");
    xml.append("          <action application=\"set\" data=\"effective_caller_id_number=${caller_id_number}\"/>\n");
    xml.append("          <action application=\"bridge\" data=\"sofia/gateway/").append(trunkName).append("/$1\"/>\n");
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