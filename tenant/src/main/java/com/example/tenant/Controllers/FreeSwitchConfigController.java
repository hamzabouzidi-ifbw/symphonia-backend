/*
package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/freeswitch")
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;

    @GetMapping("/config") // Une seule URL pour tout
    public ResponseEntity<String> generateXml(
            @RequestParam("section") String section,
            @RequestParam(name = "context", required = false) String domainName,
            @RequestParam(name = "destination_number", required = false) String destNumber,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestParam(name = "user", required = false) String user) {

        if ("dialplan".equals(section)) {
            // Logique du dialplan ici, en utilisant domainName et destNumber
            System.out.println("Requête pour DIALPLAN reçue pour le contexte : " + domainName);
            return generateDialplanXml(domainName, destNumber); // Appelez votre méthode existante
        }

        if ("directory".equals(section)) {
            // Logique du directory ici, en utilisant directoryDomain et directoryUser
            System.out.println("Requête pour DIRECTORY reçue pour le domaine : " + domain);
            return generateDirectoryXml(domain, user); // Appelez votre méthode existante
        }

        // Si la section n'est ni dialplan ni directory, retourner "not found"
        return notFoundXml();
    }

    public ResponseEntity<String> generateDialplanXml(String domainName,String destNumber) {

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

        // 3. Ajouter d'autres règles si besoin...

        xmlBuilder.append("    </context>\n");
        xmlBuilder.append("  </section>\n");
        xmlBuilder.append("</document>");
        System.out.println("generateDialplanXml appelé avec domainName=" + domainName + " et destNumber=" + destNumber);

        return ResponseEntity.ok(xmlBuilder.toString());
    }

    public ResponseEntity<String> generateDirectoryXml(String domain,String user) {

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
                        "          <variable name=\"user_context\" value=\"" + (profile.getDomainName() != null ? profile.getDomainName() : "default") + "\"/>\n" +                        "          <variable name=\"effective_caller_id_name\" value=\"" + (profile.getDomainName() != null ? profile.getDomainName() : profile.getExtension()) + "\"/>\n" +
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



    private ResponseEntity<String> notFoundXml() {
        String notFound = "<document type=\"freeswitch/xml\"><section name=\"result\"><result status=\"not found\" /></section></document>";
        return ResponseEntity.ok(notFound);
    }

}*/
package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/freeswitch" )
public class FreeSwitchConfigController {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private TenantService tenantService;

    @GetMapping("/config") // Une seule URL pour tout
    public ResponseEntity<String> generateXml(
            @RequestParam("section") String section,
            @RequestParam(name = "context", required = false) String context,
            @RequestParam(name = "destination_number", required = false) String destNumber,
            @RequestParam(name = "domain", required = false) String domain,
            @RequestParam(name = "user", required = false) String user) {

        if ("dialplan".equals(section)) {
            // Logique du dialplan ici, en utilisant domainName et destNumber
            System.out.println("Requête pour DIALPLAN reçue pour le contexte : " + context);
            return generateDialplanXml(context, destNumber); // Appelez votre méthode existante
        }

        if ("directory".equals(section)) {
            // Logique du directory ici, en utilisant directoryDomain et directoryUser
            System.out.println("Requête pour DIRECTORY reçue pour le domaine : " + domain);
            return generateDirectoryXml(domain, user); // Appelez votre méthode existante
        }

        // Si la section n'est ni dialplan ni directory, retourner "not found"
        return notFoundXml();
    }

    // Méthode privée pour le DIALPLAN. Pas d'annotations ici.
    private ResponseEntity<String> generateDialplanXml(String paramContext,String paramDestNumber) {

        Optional<Tenant> tenantOpt = tenantService.getByDomain(paramContext);

        if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
            System.out.println("Tenant non trouvé ou inactif pour le domaine : " + paramContext);
            return notFoundXml();
        }

        Tenant tenant = tenantOpt.get();
        System.out.println("Tenant trouvé : " + tenant.getDomainName() + " (ID=" + tenant.getId() + ")");

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<document type=\"freeswitch/xml\">\n");
        xmlBuilder.append("  <section name=\"dialplan\" description=\"Dynamic Dialplan\">\n");
        xmlBuilder.append("    <context name=\"").append(paramContext).append("\">\n");


        // Ajouter les variables ici
        xmlBuilder.append("      <variables>\n");
        xmlBuilder.append("          <variable name=\"user_context\" value=\"" + paramContext + "\"/>\n");
        // On vérifie que paramDestNumber n'est pas null avant de l'ajouter
        if (paramDestNumber != null) {
            xmlBuilder.append("        <variable name=\"dialed_number\" value=\"").append(paramDestNumber).append("\"/>\n");
        }
        xmlBuilder.append("      </variables>\n");



        // 1. Appels internes tenant (extensions 4 chiffres)
        String extensionPattern = "^(\\d{4})$";
        xmlBuilder.append("      <extension name=\"local_call_within_tenant\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"").append(extensionPattern).append("\">\n");
        xmlBuilder.append("          <action application=\"set\" data=\"voicemail_authorized=true\"/>\n");
        xmlBuilder.append("          <action application=\"bridge\" data=\"user/$1@").append(paramContext).append("\"/>\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"").append(paramContext).append(" $1\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");

        // 3. Ajouter d'autres règles si besoin...

        xmlBuilder.append("    </context>\n");
        xmlBuilder.append("  </section>\n");
        xmlBuilder.append("</document>");
        System.out.println("generateDialplanXml appelé avec domainName=" + paramContext + " et destNumber=" + paramDestNumber);

        return ResponseEntity.ok(xmlBuilder.toString());
    }

    // Méthode privée pour le DIRECTORY. Pas d'annotations ici.
    private ResponseEntity<String> generateDirectoryXml(String domain,String user) {
        if (domain == null || user == null) {
            System.out.println("ERREUR : Le domaine ou l'utilisateur pour le directory est null.");
            return notFoundXml();
        }
        Optional<SipProfile> sipProfileOpt = sipUserService.findByDomainAndExtension(domain, user);

        if (sipProfileOpt.isEmpty() || !sipProfileOpt.get().isActive()) {
            System.out.println("Profil SIP non trouvé pour " + user + "@" + domain);
            return notFoundXml();
        }

        SipProfile profile = sipProfileOpt.get();
        System.out.println("Profil SIP trouvé pour le directory : " + profile.getExtension());

        String xml =
                "<document type=\"freeswitch/xml\">\n" +
                        "  <section name=\"directory\">\n" +
                        "    <domain name=\"" + domain + "\">\n" +
                        "      <user id=\"" + profile.getExtension() + "\">\n" +
                        "        <params>\n" +
                        "          <param name=\"password\" value=\"" + profile.getPassword() + "\"/>\n" +
                        "        </params>\n" +
                        "        <variables>\n" +
                        "          <variable name=\"user_context\" value=\"" + profile.getDomainName() + "\"/>\n" +
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

