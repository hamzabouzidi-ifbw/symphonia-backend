package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.SipUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SipUserService userSipService;

    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody CreateTenantRequest request,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            Tenant createdTenant = tenantService.createTenant(request);
            return ResponseEntity.ok(createdTenant);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to create a tenant."));
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllTenantsWithLicences(@RequestHeader("Authorization") String token,
                                                       @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            List<TenantWithLicencesResponse> tenants = tenantService.getAllTenantsWithLicences(token);
            return ResponseEntity.ok(tenants);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to access tenants."));
        }
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<?> updateTenant(@PathVariable Long tenantId,
                                          @RequestBody UpdateTenantRequest request,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role) || "ADMIN_TENANT".equals(role)) {
            try {
                Tenant updatedTenant = tenantService.updateTenant(tenantId, request);
                return ResponseEntity.ok(updatedTenant);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'avez pas la permission de modifier ce tenant."));
        }
    }
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<?> deleteTenant(@PathVariable Long tenantId,
                                          @RequestHeader("Authorization") String token,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            try {
                tenantService.deleteTenant(tenantId, token);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'avez pas la permission de supprimer ce tenant."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        Optional<Tenant> tenant = tenantRepository.findById(id);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{tenantId}/deactivate")
    public ResponseEntity<?> deactivateTenant(
            @PathVariable Long tenantId,
            @RequestHeader("Authorization") String token,
            @RequestHeader("role") String role) {

        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to deactivate tenants"));
        }

        try {
            tenantService.deactivateTenant(tenantId, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

   /* @GetMapping(value = "/freeswitch/directory", produces = "application/xml")
    public ResponseEntity<String> getDirectory(@RequestParam Map<String, String> params) {
        String domain = params.get("domain");

        if (domain == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Tenant> tenantOpt = tenantService.getByDomain(domain);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();

        // Récupérer tous les utilisateurs SIP du tenant
        List<SipProfile> users = userSipService.getUsersByTenantId(tenant.getId());

        StringBuilder usersXml = new StringBuilder();

        for (SipProfile user : users) {
            usersXml.append("""
            <user id="%s">
              <variables>
                <variable name="extention Sip" value="%s"/>
                <variable name="domain Sip" value="%s"/>
                <variable name="email" value="%s"/>
              </variables>
            </user>
        """.formatted(
                    user.getUsername(),       // ID utilisateur SIP
                    user.getExtension(),         // Code du tenant comme accountcode
                    user.getDomainName(),  // Contexte FreeSWITCH
                    user.getEmail()          // Email de l'utilisateur SIP
            ));
        }

        // Construction du XML avec les infos du tenant
        String xml = """
        <document type="freeswitch/xml">
          <section name="directory">
            <domain name="%s">
              <user id="%s">
                <params>
                  <param name="password" value="1234"/>
                </params>
                <variables>
                  <variable name="accountcode" value="%s"/>
                  <variable name="context" value="%s"/>
                  <variable name="email" value="%s"/>
                  <variable name="phone" value="%s"/>
                  <variable name="admin_email" value="%s"/>
                  <variable name="address" value="%s"/>
                  
                  %s
                </variables>
              </user>
            </domain>
          </section>
        </document>
        """.formatted(
                tenant.getDomainName(),     // %s -> domaine
                tenant.getCode(),           // %s -> id (on prend code ici)
                tenant.getCode(),           // accountcode
                tenant.getContextName(),    // context
                tenant.getEmail(),          // email
                tenant.getPhone(),          // phone
                tenant.getAdminEmail(),     // admin_email
                tenant.getAddress(),         // address

                usersXml.toString()
        );

        return ResponseEntity.ok(xml);
    }*/


    @GetMapping(value = "/freeswitch/directory", produces = "application/xml")
    public ResponseEntity<String> getDirectory(@RequestParam Map<String, String> params) {
        String domain = params.get("domain");

        if (domain == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Tenant> tenantOpt = tenantService.getByDomain(domain);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();

        // Récupérer tous les utilisateurs SIP du tenant
        List<SipProfile> users = userSipService.getUsersByTenantId(tenant.getId());

        StringBuilder usersXml = new StringBuilder();

        for (SipProfile user : users) {
            usersXml.append("""
            <user id="%s">
              <params>
                <param name="password" value="%s"/>
              </params>
              <variables>
                <variable name="username" value="%s"/>
                <variable name="email" value="%s"/>
                <variable name="extension" value="%s"/>
                <variable name="domainName" value="%s"/>
                <variable name="tenantId" value="%s"/>
                <variable name="licenceDefinitionId" value="%s"/>
                <variable name="creationDate" value="%s"/>
                <variable name="active" value="%s"/>
              </variables>
            </user>
            """.formatted(
                    user.getUsername(),               // user id
                    user.getPassword(),               // param: SIP password
                    user.getUsername(),
                    user.getEmail(),
                    user.getExtension(),
                    user.getDomainName(),
                    String.valueOf(user.getTenantId()),
                    user.getLicenceDefinitionId() != null ? user.getLicenceDefinitionId().toString() : "",
                    user.getCreationDate() != null ? user.getCreationDate().toString() : "",
                    user.isActive()
            ));
        }

        // XML complet
        // Construction du XML avec les infos du tenant
        String xml = """
        <document type="freeswitch/xml">
          <section name="directory">
            <domain name="%s">
              <user id="%s">
                <params>
                  <param name="password" value="1234"/>
                </params>
                <variables>
                  <variable name="accountcode" value="%s"/>
                  <variable name="context" value="%s"/>
                  <variable name="email" value="%s"/>
                  <variable name="phone" value="%s"/>
                  <variable name="admin_email" value="%s"/>
                  <variable name="address" value="%s"/>
                  
                  %s
                </variables>
              </user>
            </domain>
          </section>
        </document>
        """.formatted(
                tenant.getDomainName(),     // %s -> domaine
                tenant.getCode(),           // %s -> id (on prend code ici)
                tenant.getCode(),           // accountcode
                tenant.getContextName(),    // context
                tenant.getEmail(),          // email
                tenant.getPhone(),          // phone
                tenant.getAdminEmail(),     // admin_email
                tenant.getAddress(),         // address

                usersXml.toString()
        );

        return ResponseEntity.ok(xml);
    }

   /* @GetMapping(value = "/freeswitch/dialplan", produces = "application/xml")
    public ResponseEntity<String> getDialplan(@RequestParam Map<String, String> params) {
        String context = params.get("context");
        if (context == null) return ResponseEntity.badRequest().build();

        String xml = """
    <document type="freeswitch/xml">
      <section name="dialplan">
        <context name="%s">
          <extension name="demo">
            <condition field="destination_number" expression="^1000$">
              <action application="answer"/>
              <action application="playback" data="demo-thanks"/>
              <action application="hangup"/>
            </condition>
          </extension>
        </context>
      </section>
    </document>
    """.formatted(context);

        return ResponseEntity.ok(xml);
    }
*/

    @GetMapping(value = "/freeswitch/dialplan", produces = "application/xml")
    public ResponseEntity<String> getDialplan(@RequestParam Map<String, String> params) {
        String context = params.get("context");
        if (context == null) return ResponseEntity.badRequest().build();

        // Récupérer les utilisateurs SIP actifs pour le tenant/context donné
        // Suppose que tu as une méthode pour récupérer les utilisateurs par contexte ou tenant
        // Par exemple, ici on récupère tous les utilisateurs et on filtre par contexte (à adapter selon ta logique)
        List<SipProfile> users = userSipService.getUsersByContext(context);

        if (users.isEmpty()) {
            // Pas d'utilisateur dans ce contexte, retourne un dialplan vide ou 404
            return ResponseEntity.ok("""
            <document type="freeswitch/xml">
              <section name="dialplan">
                <context name="%s"/>
              </section>
            </document>
            """.formatted(context));
        }

        // Extraire toutes les extensions des utilisateurs actifs
        List<String> extensions = users.stream()
                .filter(SipProfile::isActive)         // filtrer uniquement actifs
                .map(SipProfile::getExtension)
                .toList();

        // Construire une expression regex du type ^(ext1|ext2|ext3)$
        String regex = "^(" + String.join("|", extensions) + ")$";

        String xml = """
    <document type="freeswitch/xml">
      <section name="dialplan">
        <context name="%s">
          <extension name="internal_call">
            <condition field="destination_number" expression="%s">
              <action application="bridge" data="sofia/internal/${destination_number}@${domain_name}"/>
            </condition>
          </extension>
        </context>
      </section>
    </document>
    """.formatted(context, regex);

        return ResponseEntity.ok(xml);
    }

    //Cette méthode sert à fournir dynamiquement la configuration XML de FreeSWITCH pour un fichier spécifique
    @GetMapping(value = "/freeswitch/configuration", produces = "application/xml")
    public ResponseEntity<String> getConfiguration(@RequestParam Map<String, String> params) {
        String configName = params.get("key_value");
        if (!"sofia.conf".equals(configName)) return ResponseEntity.notFound().build();
        //sofia.conf est la configuration principale du module SIP sofia de FreeSWITCH.

        String xml = """
    <document type="freeswitch/xml">
      <section name="configuration">
        <configuration name="sofia.conf" description="sofia dynamic config">
          <profiles>
            <profile name="internal">
              <gateways>
                <gateway name="my-gateway">
                  <param name="username" value="1000"/>
                  <param name="password" value="1234"/>
                </gateway>
              </gateways>
            </profile>
          </profiles>
        </configuration>
      </section>
    </document>
    """;

        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/freeswitch/phrases", produces = "application/xml")
    public ResponseEntity<String> getPhrases() {
        String xml = """
    <document type="freeswitch/xml">
      <section name="phrases"/>
    </document>
    """;
        return ResponseEntity.ok(xml);
    }

}