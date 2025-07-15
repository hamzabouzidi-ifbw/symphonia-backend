package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/freeswitch")
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private DidNumberService didNumberService;

    @GetMapping(value = "/config", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getFreeSwitchConfig(
            @RequestParam String section,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String user,
            @RequestParam(required = false, name = "context") String contextName,
            @RequestParam(required = false, name = "destination_number") String destNumber) {

        if ("directory".equals(section)) {
            if (domain != null && user != null) {
                return generateDirectoryXml(domain, user);
            }
        } else if ("dialplan".equals(section)) {
            if (contextName != null && destNumber != null) {
                return generateDialplanXml(contextName, destNumber);
            }
        }

        return notFoundXml();
    }


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


        xmlBuilder.append("      <extension name=\"check_voicemail\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^123$\">\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"check default ").append(domainName).append("\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");


        // 1. Appels internes au tenant (extensions à 4 chiffres)
        String extensionPattern = "^(\\d{4})$";
        xmlBuilder.append("      <extension name=\"local_call_within_tenant\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"").append(extensionPattern).append("\">\n");
        xmlBuilder.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
        xmlBuilder.append("          <action application=\"bridge\" data=\"user/$1@").append(domainName).append("\"/>\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"default ").append(domainName).append(" $1\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");


        // 2. Appels entrants via un DID
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

                    xmlBuilder.append("      <extension name=\"incoming_did\">\n");
                    xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^").append(destNumber).append("$\">\n");
                    xmlBuilder.append("          <action application=\"bridge\" data=\"user/").append(sipExtension).append("@").append(domainName).append("\"/>\n");
                    xmlBuilder.append("          <action application=\"voicemail\" data=\"default ").append(domainName).append(" ").append(sipExtension).append("\"/>\n");
                    xmlBuilder.append("        </condition>\n");
                    xmlBuilder.append("      </extension>\n");

                    System.out.println("Extension DID ajoutée dans le dialplan XML.");
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


    private ResponseEntity<String> generateDirectoryXml(String domainName, String username) {
        Optional<SipProfile> sipProfileOpt = sipUserService.findByDomainAndUsername(domainName, username);

        if (sipProfileOpt.isEmpty() || !sipProfileOpt.get().isActive()) {
            return notFoundXml();
        }

        SipProfile profile = sipProfileOpt.get();

        String xml = "<document type=\"freeswitch/xml\">\n" +
                "  <section name=\"directory\">\n" +
                "    <domain name=\"" + profile.getDomainName() + "\">\n" +
                "      <user id=\"" + profile.getUsername() + "\">\n" +
                "        <params>\n" +
                "          <param name=\"password\" value=\"" + profile.getPassword() + "\"/>\n" +
                "        </params>\n" +
                "        <variables>\n" +
                "          <variable name=\"user_context\" value=\"" + profile.getDomainName() + "\"/>\n" +
                "          <variable name=\"effective_caller_id_name\" value=\"" + profile.getUsername() + "\"/>\n" +
                "          <variable name=\"effective_caller_id_number\" value=\"" + profile.getExtension() + "\"/>\n" +
                "          <variable name=\"vm-password\" value=\"" + profile.getExtension() + "\"/>\n" +
                "          <variable name=\"vm-mailto\" value=\"" + profile.getEmail() + "\"/>\n" +
                "        </variables>\n" +
                "      </user>\n" +
                "    </domain>\n" +
                "  </section>\n" +
                "</document>";

        return ResponseEntity.ok(xml);
    }

    private ResponseEntity<String> notFoundXml() {
        String notFound = "<document type=\"freeswitch/xml\"><section name=\"result\"><result status=\"not found\" /></section></document>";
        return ResponseEntity.ok(notFound);
    }
}