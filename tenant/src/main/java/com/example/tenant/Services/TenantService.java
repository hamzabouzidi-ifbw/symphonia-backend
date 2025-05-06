package com.example.tenant.Services;




import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Dto.RegisterUserRequest;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

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

    public void createTenant(CreateTenantRequest request) {
        // 1. Créer le tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        String code = "T-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        tenant.setCode(code);

        tenantRepository.save(tenant);

     // 2. Générer mot de passe aléatoire
        String password = UUID.randomUUID().toString().substring(0, 10);

        // 3. Envoyer les infos à auth-service
        RegisterUserRequest userRequest = new RegisterUserRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(password);
        userRequest.setRole("ADMIN_TENANT");
        userRequest.setTenantId(tenant.getId());

        restTemplate.postForObject(authServiceUrl + "/authentification/register_tenant", userRequest, String.class);

        // 4. Envoyer le mail à l'admin tenant
        emailService.sendCredentials(request.getAdminEmail(), password);
    }
}
