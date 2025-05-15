package com.example.tenant.Services;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

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
        // 1. Vérifications uniques
        if (tenantRepository.findByName(request.getName()).isPresent())
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        if (tenantRepository.findByDomain(request.getDomain()).isPresent())
            throw new RuntimeException("Un tenant avec ce domaine existe déjà.");
        if (tenantRepository.findByEmail(request.getEmail()).isPresent())
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        if (tenantRepository.findByAdminEmail(request.getAdminEmail()).isPresent())
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");

        // 2. Création du tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCompanyName(request.getCompanyName());
        tenant.setAddress(request.getAddress());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setDomain(request.getDomain());
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setCode(generateTenantCode(request.getName()));

        Tenant savedTenant = tenantRepository.save(tenant);

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

        } catch (Exception e) {
            tenantRepository.delete(savedTenant);
            // (Optionnel) : rollback licences ici via un appel delete à licence-service
            throw new RuntimeException("Échec de la création de l'utilisateur admin. Le tenant a été supprimé.", e);
        }

        return savedTenant;
    }

}
