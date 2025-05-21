package com.example.tenant.Services;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RestTemplate restTemplate;

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
            // Prendre les 3 premiers octets pour un code court et unique
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


        System.out.print( request.getTenantName());
        // 1. Vérifications de l'unicité
        if (tenantRepository.findByTenantName( request.getTenantName()).isPresent())
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        if (tenantRepository.findByEmail(request.getEmail()).isPresent())
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        if (tenantRepository.findByAdminEmail(request.getAdminEmail()).isPresent())
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");

        // 2. Génération automatique des champs
        String domainName =  request.getTenantName() + "@symphonia.com";
        String contextName =  request.getTenantName() + "_context";
        String code =  request.getTenantName() + "-" + String.format("%03d", new Random().nextInt(1000));

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
        System.out.print(savedTenant.getTenantName());
        // 3. Appel au microservice de licences
        try {
            MultipleLicenceAssignmentRequest licenceRequest = new MultipleLicenceAssignmentRequest();
            licenceRequest.setTenantId(savedTenant.getId());
            for (LicenceAssignmentRequest licence : request.getLicences()) {
                licence.setTenantId(savedTenant.getId());
            }

            licenceRequest.setLicences(request.getLicences());

            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MultipleLicenceAssignmentRequest> entity = new HttpEntity<>(licenceRequest, headers);

            restTemplate.postForEntity(
                    licenceServiceUrl + "/assign-multiple",
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            // En cas d'erreur, rollback du tenant
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

            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RegisterUserRequest> entity = new HttpEntity<>(userRequest, headers);

            restTemplate.postForObject(authServiceUrl + "/authentification/register_tenant", entity, String.class);

            // Envoi des credentials
            emailService.sendCredentials(request.getAdminEmail(), password);
            System.out.print(request);

        } catch (Exception e) {
            tenantRepository.delete(savedTenant);
            // (Optionnel) : rollback licences ici via un appel delete à licence-service
            throw new RuntimeException("Échec de la création de l'utilisateur admin. Le tenant a été supprimé.", e);
        }

        return savedTenant;
    }


    // affichage des tenants avec leurs licences
    public List<TenantWithLicencesResponse> getAllTenantsWithLicences(String token) {
        List<Tenant> tenants = tenantRepository.findAll();
        List<TenantWithLicencesResponse> result = new ArrayList<>();

        for (Tenant tenant : tenants) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<LicenceAssignmentRequest[]> response = restTemplate
                        .exchange(
                                licenceServiceUrl + "/by-tenant/" + tenant.getId(),
                                HttpMethod.GET,
                                entity,
                                LicenceAssignmentRequest[].class
                        );

                TenantWithLicencesResponse item = new TenantWithLicencesResponse();
                item.setTenant(tenant);
                item.setLicences(List.of(response.getBody()));

                result.add(item);
            } catch (Exception e) {
                // En cas d'erreur, log ou ignorer ce tenant
                e.printStackTrace();
            }
        }

        return result;
    }
    public Tenant updateTenant(Long tenantId, UpdateTenantRequest request) {
        // 1. Récupérer le tenant existant
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        if (optionalTenant.isEmpty()) {
            throw new RuntimeException("Tenant non trouvé avec l'ID: " + tenantId);
        }

        Tenant existingTenant = optionalTenant.get();

        // 2. Vérifier les champs uniques (sauf pour le tenant actuel)
        if (tenantRepository.findByTenantNameAndIdNot(request.getTenantName(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec ce nom existe déjà.");
        }

        if (tenantRepository.findByDomainNameAndIdNot(request.getDomainName(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec ce domaine existe déjà.");
        }

        if (tenantRepository.findByEmailAndIdNot(request.getEmail(), tenantId).isPresent()) {
            throw new RuntimeException("Un autre tenant avec cet email existe déjà.");
        }

        // 3. Mettre à jour les champs modifiables
        existingTenant.setTenantName(request.getTenantName());
        existingTenant.setCode(request.getCode());
        existingTenant.setContextName(request.getContextName());
        existingTenant.setAddress(request.getAddress());
        existingTenant.setEmail(request.getEmail());
        existingTenant.setPhone(request.getPhone());
        existingTenant.setDomainName(request.getDomainName());

        // Note: On ne met pas à jour le code (généré automatiquement) ni l'email admin (lié à l'authentification)

        // 4. Sauvegarder les modifications
        return tenantRepository.save(existingTenant);
    }
    public void deleteTenant(Long tenantId, String token) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant non trouvé avec l'ID: " + tenantId));

        try {
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", token);
            // Add the role header

            ResponseEntity<Void> authResponse = restTemplate.exchange(
                    authServiceUrl + "/authentification/users/by-tenant/" + tenantId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders),
                    Void.class
            );

            if (!authResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de la suppression des utilisateurs - Code: " + authResponse.getStatusCode());
            }

            // 2. Suppression des licences
            HttpHeaders licenceHeaders = new HttpHeaders();
            licenceHeaders.set("Authorization", token);

            ResponseEntity<Void> licenceResponse = restTemplate.exchange(
                    licenceServiceUrl + "/by-tenant/" + tenantId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(licenceHeaders),
                    Void.class
            );

            if (!licenceResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de la suppression des licences - Code: " + licenceResponse.getStatusCode());
            }

            // 3. Finalement supprimer le tenant
            tenantRepository.delete(tenant);

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Erreur client: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Erreur serveur: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue: " + e.getMessage());
        }
    }
}