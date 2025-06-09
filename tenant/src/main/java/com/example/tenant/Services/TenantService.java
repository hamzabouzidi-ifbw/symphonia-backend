package com.example.tenant.Services;

import com.example.tenant.Clients.AuthServiceClient;
import com.example.tenant.Clients.LicenceServiceClient;
import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


import java.nio.file.*;
import java.io.*;
@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private LicenceServiceClient licenceServiceClient;

    @Autowired
    private EmailService emailService;

    @Value("${licence.service.url}")
    private String licenceServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private String generateTenantCode(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(name.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder code = new StringBuilder("T-");
            for (int i = 0; i < 3; i++) {
                code.append(String.format("%02X", hash[i]));
            }
            return code.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors de la génération du code tenant", e);
        }
    }

    public Tenant createTenant(CreateTenantRequest request) {
        // 1. Vérifications de l'unicité
        if (tenantRepository.findByTenantName(request.getTenantName()).isPresent())
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        if (tenantRepository.findByEmail(request.getEmail()).isPresent())
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        if (tenantRepository.findByAdminEmail(request.getAdminEmail()).isPresent())
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");

        // 2. Génération automatique des champs
        String domainName = request.getTenantName() + "@symphonia.com";
        String contextName = request.getTenantName() + "_context";
        String code = request.getTenantName() + "-" + String.format("%03d", new Random().nextInt(1000));

        // 3. Création de l'entité Tenant
        Tenant tenant = new Tenant();
        tenant.setTenantName(request.getTenantName());
        tenant.setAddress(request.getAddress());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setDomainName(domainName);
        tenant.setContextName(contextName);
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setCode(code);

        Tenant savedTenant = tenantRepository.save(tenant);

        // dialplan for tenant
        generateDialplanForTenant(savedTenant);
        generateDirectoryForTenant(savedTenant);
        reloadFreeSwitchXml();

        // Get authorization token from request
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpRequest.getHeader("Authorization");

        // 3. Appel au microservice de licences
        try {
            MultipleLicenceAssignmentRequest licenceRequest = new MultipleLicenceAssignmentRequest();
            licenceRequest.setTenantId(savedTenant.getId());
            for (LicenceAssignmentRequest licence : request.getLicences()) {
                licence.setTenantId(savedTenant.getId());
            }
            licenceRequest.setLicences(request.getLicences());

            licenceServiceClient.assignMultipleLicences(token, licenceRequest);
        } catch (Exception e) {
            tenantRepository.delete(savedTenant);
            throw new RuntimeException("Échec de l'assignation des licences. Le tenant a été supprimé.", e);
        }

        // 4. Génération du mot de passe admin et enregistrement dans auth-service
        try {
            String password = UUID.randomUUID().toString().substring(0, 10);

            RegisterUserRequest userRequest = new RegisterUserRequest();
            userRequest.setEmail(request.getAdminEmail());
            userRequest.setPassword(password);
            userRequest.setRole("ADMIN_TENANT");
            userRequest.setTenantId(savedTenant.getId());

            authServiceClient.registerTenantUser(token, userRequest);

            // Envoi des credentials
            emailService.sendCredentials(request.getAdminEmail(), password);
        } catch (Exception e) {
            tenantRepository.delete(savedTenant);
            throw new RuntimeException("Échec de la création de l'utilisateur admin. Le tenant a été supprimé.", e);
        }

        return savedTenant;
    }

    public List<TenantWithLicencesResponse> getAllTenantsWithLicences(String token) {
        List<Tenant> tenants = tenantRepository.findAll();
        List<TenantWithLicencesResponse> result = new ArrayList<>();

        for (Tenant tenant : tenants) {
            try {
                List<LicenceAssignmentRequest> licences = licenceServiceClient.getLicencesByTenant(token, tenant.getId());

                TenantWithLicencesResponse item = new TenantWithLicencesResponse();
                item.setTenant(tenant);
                item.setLicences(licences);

                result.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public Tenant updateTenant(Long tenantId, UpdateTenantRequest request) {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        if (optionalTenant.isEmpty()) {
            throw new RuntimeException("Tenant non trouvé avec l'ID: " + tenantId);
        }

        Tenant existingTenant = optionalTenant.get();

        if (tenantRepository.findByTenantNameAndIdNot(request.getTenantName(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec ce nom existe déjà.");
        }

        if (tenantRepository.findByDomainNameAndIdNot(request.getDomainName(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec ce domaine existe déjà.");
        }

        if (tenantRepository.findByEmailAndIdNot(request.getEmail(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec cet email existe déjà.");
        }

        existingTenant.setTenantName(request.getTenantName());
        existingTenant.setCode(request.getCode());
        existingTenant.setContextName(request.getContextName());
        existingTenant.setAddress(request.getAddress());
        existingTenant.setEmail(request.getEmail());
        existingTenant.setPhone(request.getPhone());
        existingTenant.setDomainName(request.getDomainName());

        return tenantRepository.save(existingTenant);
    }

    public void deleteTenant(Long tenantId, String token) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant non trouvé avec l'ID: " + tenantId));

        try {
            // 1. Suppression des utilisateurs
            authServiceClient.deleteUsersByTenant(token, tenantId);

            // 2. Suppression des licences
            licenceServiceClient.deleteLicencesByTenant(token, tenantId);

            // 3. Finalement supprimer le tenant
            tenantRepository.delete(tenant);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du tenant: " + e.getMessage());
        }
    }


    // dialplan for tenant
    private void generateDialplanForTenant(Tenant tenant) {
        String contextName = tenant.getContextName();

        String contextXml = """
        <include>
            <context name="%s">
                <extension name="default">
                    <condition field="destination_number" expression="^(\\d+)$">
                        <action application="answer"/>
                        <action application="playback" data="ivr/ivr-welcome_to_freeswitch.wav"/>
                        <action application="hangup"/>
                    </condition>
                </extension>
            </context>
        </include>
        """.formatted(contextName);

        Path contextPath = Paths.get("/etc/freeswitch/dialplan/" + contextName + ".xml");
        try {
            Files.writeString(contextPath, contextXml);
            System.out.println("✅ Dialplan écrit dans : " + contextPath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du dialplan", e);
        }
    }

    private void generateDirectoryForTenant(Tenant tenant) {
        String domainName = tenant.getDomainName();
        String contextName = tenant.getContextName();

        String directoryXml = """
        <domain name="%s">
            <params>
                <param name="dial-string" value="{context=%s}${sofia_contact(${dialed_user}@${dialed_domain})}"/>
            </params>
            <users>
                <X-PRE-PROCESS cmd="include" data="default.xml"/>
            </users>
        </domain>
        """.formatted(domainName, contextName);

        Path directoryPath = Paths.get("/etc/freeswitch/directory/" + domainName + ".xml");
        try {
            Files.writeString(directoryPath, directoryXml);
            System.out.println("✅ Directory écrit dans : " + directoryPath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du fichier directory", e);
        }
    }

    private void reloadFreeSwitchXml() {
        try {
            Process process = Runtime.getRuntime().exec("fs_cli -x reloadxml");
            process.waitFor();
            System.out.println("🔄 FreeSWITCH rechargé (reloadxml)");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du reloadxml de FreeSWITCH", e);
        }
    }

}