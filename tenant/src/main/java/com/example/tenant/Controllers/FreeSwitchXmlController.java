package com.example.tenant.Controllers;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/freeswitch/xml")
public class FreeSwitchXmlController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private SipUserService sipUserService;

    @GetMapping
    public ResponseEntity<String> getXml(
            @RequestParam String section,
            @RequestParam String domain,
            @RequestParam(required = false) String user) {

        if ("directory".equalsIgnoreCase(section)) {
            return ResponseEntity.ok(generateDirectoryXml(domain, user));
        } else if ("dialplan".equalsIgnoreCase(section)) {
            return ResponseEntity.ok(generateDialplanXml(domain));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Section inconnue");
    }

    private String generateDirectoryXml(String domain, String user) {
        Optional<Tenant> tenantOpt = tenantService.getByDomain(domain);
        if (tenantOpt.isEmpty()) {
            return emptyDirectoryResponse(domain);
        }
        Tenant tenant = tenantOpt.get();

        List<SipProfile> users;
        if (user != null && !user.isEmpty()) {
            users = sipUserService.findByDomainAndUsername(domain, user)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            users = sipUserService.getSipUsersByDomain(domain);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<document type=\"freeswitch/xml\">\n");
        sb.append("<section name=\"directory\">\n");
        sb.append("<domain name=\"" + domain + "\">\n");

        for (SipProfile u : users) {
            sb.append("<user id=\"" + u.getExtension() + "\">\n");
            sb.append("  <params>\n");
            sb.append("    <param name=\"password\" value=\"" + u.getPassword() + "\"/>\n");
            sb.append("    <param name=\"vm-password\" value=\"" + u.getPassword() + "\"/>\n");
            sb.append("  </params>\n");
            sb.append("  <variables>\n");
            sb.append("    <variable name=\"user_context\" value=\"" + tenant.getContextName() + "\"/>\n");
            sb.append("    <variable name=\"accountcode\" value=\"" + domain + "\"/>\n");
            sb.append("  </variables>\n");
            sb.append("</user>\n");
        }

        sb.append("</domain>\n</section>\n</document>\n");
        return sb.toString();
    }

    private String emptyDirectoryResponse(String domain) {
        return "<?xml version=\"1.0\"?>\n"
                + "<document type=\"freeswitch/xml\">\n"
                + "<section name=\"directory\">\n"
                + "<domain name=\"" + domain + "\" />\n"
                + "</section>\n"
                + "</document>";
    }

    private String generateDialplanXml(String domain) {
        Optional<Tenant> tenantOpt = tenantService.getByDomain(domain);
        if (tenantOpt.isEmpty()) {
            return emptyDialplanResponse(domain);
        }
        Tenant tenant = tenantOpt.get();
        String context = tenant.getContextName();

        return "<?xml version=\"1.0\"?>\n" +
                "<document type=\"freeswitch/xml\">\n" +
                "<section name=\"dialplan\">\n" +
                "<context name=\"" + context + "\">\n" +
                "    <extension name=\"default\">\n" +
                "        <condition field=\"destination_number\" expression=\"^(\\d{3})$\">\n" +
                "            <action application=\"answer\" />\n" +
                "            <action application=\"playback\" data=\"welcome.wav\" />\n" +
                "            <action application=\"hangup\" />\n" +
                "        </condition>\n" +
                "    </extension>\n" +
                "</context>\n" +
                "</section>\n" +
                "</document>";
    }

    private String emptyDialplanResponse(String domain) {
        return "<?xml version=\"1.0\"?>\n"
                + "<document type=\"freeswitch/xml\">\n"
                + "<section name=\"dialplan\">\n"
                + "</section>\n"
                + "</document>";
    }
}

