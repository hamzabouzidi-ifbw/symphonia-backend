package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/config", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getFreeSwitchConfig(
            // Paramètres envoyés par FreeSWITCH
            @RequestParam String section,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String user,
            @RequestParam(required = false, name = "context") String contextName,
            @RequestParam(required = false, name = "destination_number") String destNumber) {

        if ("directory".equals(section)) {
            // Gestion de l'annuaire (inchangée)
            if (domain != null && user != null) {
                return generateDirectoryXml(domain, user);
            }
        } else if ("dialplan".equals(section)) {
            // NOUVEAU : Gestion du dialplan
            if (contextName != null && destNumber != null) {
                return generateDialplanXml(contextName, destNumber);
            }
        }

        // Réponse par défaut si la section n'est pas gérée
        return notFoundXml();
    }

    private ResponseEntity<String> generateDialplanXml(String domainName, String destNumber) {
        // Le contexte est le nom de domaine du tenant. On vérifie si ce tenant existe.
        Optional<Tenant> tenantOpt = tenantService.getByDomain(domainName);

        if (tenantOpt.isEmpty() || !tenantOpt.get().isActive()) {
            // Si le tenant n'existe pas ou est inactif, on ne fournit aucune règle d'appel.
            return notFoundXml();
        }

        Tenant tenant = tenantOpt.get();

        // On génère le XML du dialplan pour ce contexte
        // Le nom du contexte dans le XML doit correspondre à celui demandé par FreeSWITCH
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<document type=\"freeswitch/xml\">\n");
        xmlBuilder.append("  <section name=\"dialplan\" description=\"Dynamic Dialplan\">\n");
        xmlBuilder.append("    <context name=\"").append(domainName).append("\">\n");

        // Règle 1 : Appels internes au tenant (ex: appeler une extension à 3 chiffres)
        // On utilise les informations du tenant pour construire l'expression régulière
        String extensionPattern = "^(\\d{" + 4 + "})$";
        xmlBuilder.append("      <extension name=\"local_call_within_tenant\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"").append(extensionPattern).append("\">\n");
        xmlBuilder.append("          <action application=\"bridge\" data=\"user/${destination_number}@").append(tenant.getDomainName()).append("\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");

        // Règle 2 : Appeler la messagerie vocale (exemple)
        xmlBuilder.append("      <extension name=\"voicemail\">\n");
        xmlBuilder.append("        <condition field=\"destination_number\" expression=\"^\\*97$\">\n");
        xmlBuilder.append("          <action application=\"voicemail\" data=\"default@").append(tenant.getDomainName()).append(" ${caller_id_number}\"/>\n");
        xmlBuilder.append("        </condition>\n");
        xmlBuilder.append("      </extension>\n");

        // Vous pouvez ajouter ici d'autres règles : appels sortants, conférences, etc.

        xmlBuilder.append("    </context>\n");
        xmlBuilder.append("  </section>\n");
        xmlBuilder.append("</document>");

        return ResponseEntity.ok(xmlBuilder.toString());
    }

    private ResponseEntity<String> generateDirectoryXml(String domainName, String username) {
        // ... (code inchangé)
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
                "          <variable name=\"tenant_id\" value=\"" + profile.getTenantId().toString() + "\"/>\n" +
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
