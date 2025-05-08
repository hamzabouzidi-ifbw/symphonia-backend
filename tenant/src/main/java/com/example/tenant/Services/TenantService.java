package com.example.tenant.Services;

import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Dto.RegisterUserRequest;
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
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

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

    public void createTenant(CreateTenantRequest request) {
        // 1. Vérifier si le tenant existe déjà par nom
        Optional<Tenant> existingTenantByName = tenantRepository.findByName(request.getName());
        if (existingTenantByName.isPresent()) {
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        }

        // 2. Vérifier si le domaine existe déjà
        Optional<Tenant> existingTenantByDomain = tenantRepository.findByDomain(request.getDomain());
        if (existingTenantByDomain.isPresent()) {
            throw new RuntimeException("Un tenant avec ce domaine existe déjà.");
        }

        // 3. Vérifier si l'email existe déjà
        Optional<Tenant> existingTenantByEmail = tenantRepository.findByEmail(request.getEmail());
        if (existingTenantByEmail.isPresent()) {
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        }

        // 4. Vérifier si l'email admin est déjà utilisé par un autre tenant
        Optional<Tenant> existingTenantByAdminEmail = tenantRepository.findByAdminEmail(request.getAdminEmail());
        if (existingTenantByAdminEmail.isPresent()) {
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");
        }

        // 5. Créer le tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCompanyName(request.getCompanyName());
        tenant.setAddress(request.getAddress());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setDomain(request.getDomain());
        tenant.setAdminEmail(request.getAdminEmail());

        String code = generateTenantCode(request.getName());
        tenant.setCode(code);

        tenantRepository.save(tenant);

        // 6. Générer mot de passe
        String password = UUID.randomUUID().toString().substring(0, 10);

        // 7. Envoyer à auth-service
        RegisterUserRequest userRequest = new RegisterUserRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(password);
        userRequest.setRole("ADMIN_TENANT");
        userRequest.setTenantId(tenant.getId());

        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RegisterUserRequest> entity = new HttpEntity<>(userRequest, headers);

            restTemplate.postForObject(authServiceUrl + "/authentification/register_tenant", entity, String.class);

        } catch (Exception e) {
            // En cas d'échec, supprimer le tenant créé
            tenantRepository.delete(tenant);
            throw new RuntimeException("Échec de la création de l'utilisateur admin. Le tenant a été supprimé.");
        }

        // 8. Envoyer mail
        emailService.sendCredentials(request.getAdminEmail(), password);
    }
}