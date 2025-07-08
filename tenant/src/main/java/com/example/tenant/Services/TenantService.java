package com.example.tenant.Services;

import com.example.tenant.Clients.AuthServiceClient;
import com.example.tenant.Clients.LicenceServiceClient;
import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.HttpURLConnection;
import java.net.URL;
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


    @Transactional
    public Tenant createTenant(CreateTenantRequest request) {
        if (tenantRepository.findByTenantName(request.getTenantName()).isPresent())
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        if (tenantRepository.findByEmail(request.getEmail()).isPresent())
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        if (tenantRepository.findByAdminEmail(request.getAdminEmail()).isPresent())
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");
        if (tenantRepository.existsByExtensionPrefix(request.getExtensionPrefix())) {
            throw new RuntimeException("Ce préfixe d'extension est déjà utilisé par un autre tenant");
        }
        String domainName = request.getTenantName() + "@symphonia.com";
        String contextName = request.getTenantName() + "_context";
        String code = request.getTenantName() + "-" + String.format("%03d", new Random().nextInt(1000));

        Tenant tenant = new Tenant();
        tenant.setExtensionPrefix(request.getExtensionPrefix()); // Doit être fourni dans la requête
        tenant.setExtensionLength(3); // Ou le faire configurable via request
        tenant.setNextExtensionNumber(1);
        tenant.setTenantName(request.getTenantName());
        tenant.setAddress(request.getAddress());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setDomainName(domainName);
        tenant.setContextName(contextName);
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setCode(code);

        Tenant savedTenant = tenantRepository.save(tenant);

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpRequest.getHeader("Authorization");

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

        try {
            String password = UUID.randomUUID().toString().substring(0, 10);

            RegisterUserRequest userRequest = new RegisterUserRequest();
            userRequest.setEmail(request.getAdminEmail());
            userRequest.setPassword(password);
            userRequest.setRole("ADMIN_TENANT");
            userRequest.setTenantId(savedTenant.getId());

            authServiceClient.registerTenantUser(token, userRequest);
            emailService.sendCredentials(request.getAdminEmail(), password);
        } catch (Exception e) {
            tenantRepository.delete(savedTenant);
            throw new RuntimeException("Échec de la création de l'utilisateur admin. Le tenant a été supprimé.", e);
        }

        // 👉 Appel à FreeSWITCH via HTTP (si tu veux déclencher un cache/update)

        /*Ce bloc de code Java effectue un appel HTTP vers un endpoint local (http://localhost:8083/...)
         afin de notifier FreeSWITCH (un serveur de téléphonie open source)
         pour déclencher une mise à jour ou une régénération du XML
                (probablement un fichier de configuration ou d'annuaire).*/
        try {


            // Appelle ton propre endpoint pour déclencher la génération XML
            URL url = new URL("http://localhost:8083/tenant/freeswitch/directory?domain=" + domainName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            // Ajouter le token d’authentification dans l’en-tête Authorization
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", token);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Erreur lors de la notification FreeSWITCH: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
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

        Tenant updatedTenant = tenantRepository.save(existingTenant);

        // 🟡 Récupération du token pour l'appel
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpRequest.getHeader("Authorization");

        // ✅ Appel pour notifier FreeSWITCH
        //notifyFreeSWITCHDirectoryUpdate(updatedTenant.getDomainName(), token);

        return updatedTenant;



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

            // ✅ Appel pour notifier FreeSWITCH après suppression
           // notifyFreeSWITCHDirectoryUpdate(tenant.getDomainName(), token);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du tenant: " + e.getMessage());
        }
    }

    public Optional<Tenant> getByDomain(String domain) {
        return tenantRepository.findByDomainName(domain);
    }

    private void notifyFreeSWITCHDirectoryUpdate(String domainName, String token) {
        try {
            URL url = new URL("http://localhost:8083/tenant/freeswitch/directory?domain=" + domainName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", token);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Erreur lors de la notification FreeSWITCH: " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Optional<Tenant> getByContextName(String contextName) {
        return tenantRepository.findByContextName(contextName);
    }

    @Transactional
    public void deactivateTenant(Long tenantId, String token) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // 1. Désactiver le tenant
        tenant.setActive(false);
        tenantRepository.save(tenant);

        // 2. Désactiver tous les utilisateurs associés à ce tenant
        try {
            authServiceClient.deactivateUsersByTenant(token, tenantId);

        } catch (FeignException e) {
            throw new RuntimeException("Failed to deactivate users for tenant: " + e.contentUTF8(), e);
        }
    }

    @Transactional
    public void activateTenant(Long tenantId, String token) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setActive(true);
        tenantRepository.save(tenant);

        try {
            authServiceClient.activateUsersByTenant(token, tenantId);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to activate users for tenant: " + e.contentUTF8(), e);

        }
    }
}