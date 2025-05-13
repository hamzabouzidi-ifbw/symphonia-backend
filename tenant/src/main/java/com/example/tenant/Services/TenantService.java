package com.example.tenant.Services;

import com.example.tenant.Dto.AssignLicenseRequest;
import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Dto.RegisterUserRequest;
import com.example.tenant.Dto.TenantWithLicensesResponse;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    @Value("${license.service.url}")
    private String licenseServiceUrl;

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

    public void createTenant(CreateTenantRequest request) {
        // Vérifier si le tenant existe déjà par nom
        Optional<Tenant> existingTenantByName = tenantRepository.findByName(request.getName());
        if (existingTenantByName.isPresent()) {
            throw new RuntimeException("Un tenant avec ce nom existe déjà.");
        }

        // Vérifier si le domaine existe déjà
        Optional<Tenant> existingTenantByDomain = tenantRepository.findByDomain(request.getDomain());
        if (existingTenantByDomain.isPresent()) {
            throw new RuntimeException("Un tenant avec ce domaine existe déjà.");
        }

        // Vérifier si l'email existe déjà
        Optional<Tenant> existingTenantByEmail = tenantRepository.findByEmail(request.getEmail());
        if (existingTenantByEmail.isPresent()) {
            throw new RuntimeException("Un tenant avec cet email existe déjà.");
        }

        // Vérifier si l'email admin est déjà utilisé par un autre tenant
        Optional<Tenant> existingTenantByAdminEmail = tenantRepository.findByAdminEmail(request.getAdminEmail());
        if (existingTenantByAdminEmail.isPresent()) {
            throw new RuntimeException("Cet email admin est déjà utilisé par un autre tenant.");
        }

        // Créer le tenant
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

        // Générer mot de passe
        String password = UUID.randomUUID().toString().substring(0, 10);

        // Créer la requête pour l'authentification
        RegisterUserRequest userRequest = new RegisterUserRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(password);
        userRequest.setRole("ADMIN_TENANT");
        userRequest.setTenantId(tenant.getId());

        try {
            // Récupérer le token d'autorisation de la requête en cours
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            // Vérifier que le token commence par "Bearer " et ajouter le préfixe si nécessaire
            if (token != null && !token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }

            // Créer les headers pour la requête d'authentification
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Créer l'entité pour la requête d'authentification
            HttpEntity<RegisterUserRequest> entity = new HttpEntity<>(userRequest, headers);

            // Envoyer la requête à l'auth service pour créer l'utilisateur admin
            restTemplate.postForObject(authServiceUrl + "/authentification/register_tenant", entity, String.class);

        } catch (Exception e) {
            // En cas d'échec, supprimer le tenant créé
           // tenantRepository.delete(tenant);
            throw new RuntimeException("Échec de la création de l'utilisateur admin.");
        }

        // Envoyer le mot de passe à l'email de l'admin
        emailService.sendCredentials(request.getAdminEmail(), password);

        try {
            // Créer la requête pour affecter des licences
            AssignLicenseRequest licenseRequest = new AssignLicenseRequest();
            licenseRequest.setTenantId(tenant.getId());
            licenseRequest.setLicenseKeys(request.getLicenseKeys());  // Utiliser les clés de licence envoyées dans la requête
            licenseRequest.setStartDate(LocalDate.now());
            licenseRequest.setEndDate(LocalDate.now().plusYears(1)); // 1 an de licence
            licenseRequest.setMaxUsers(100); // Nombre maximal d'utilisateurs par défaut


            // Récupérer le token d'autorisation de la requête en cours
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            // Vérifier que le token commence par "Bearer " et ajouter le préfixe si nécessaire
            if (token != null && !token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
            // Créer les headers pour la requête de licences

            HttpHeaders licenseHeaders = new HttpHeaders();
            licenseHeaders.set("Authorization", token);
            licenseHeaders.setContentType(MediaType.APPLICATION_JSON);

            // Créer l'entité pour la requête d'affectation de licence
            HttpEntity<AssignLicenseRequest> licenseEntity = new HttpEntity<>(licenseRequest, licenseHeaders);

            // Envoyer la requête à l'API de licences pour affecter les licences
            restTemplate.postForObject(licenseServiceUrl + "/assign", licenseEntity, String.class);

        } catch (Exception e) {
            // Loger l'erreur, mais ne pas interrompre la création du tenant
            System.err.println("Échec de l'affectation de licence : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TenantWithLicensesResponse> getAllTenantsWithLicenses() {
        List<Tenant> tenants = tenantRepository.findAll();
        List<TenantWithLicensesResponse> result = new ArrayList<>();

        for (Tenant tenant : tenants) {
            TenantWithLicensesResponse response = new TenantWithLicensesResponse();
            response.setId(tenant.getId());
            response.setName(tenant.getName());
            response.setDomain(tenant.getDomain());
            response.setEmail(tenant.getEmail());
            response.setAdminEmail(tenant.getAdminEmail());
            response.setCode(tenant.getCode());
            response.setAddress(tenant.getAddress());
            response.setCompanyName(tenant.getCompanyName());
            response.setPhone(tenant.getPhone());

            result.add(response);
        }

        return result;
    }

    public TenantWithLicensesResponse getTenantById(Long id) {
        Optional<Tenant> tenantOpt = tenantRepository.findById(id);
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant non trouvé avec l'ID : " + id);
        }

        Tenant tenant = tenantOpt.get();
        TenantWithLicensesResponse response = new TenantWithLicensesResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setDomain(tenant.getDomain());
        response.setEmail(tenant.getEmail());
        response.setAdminEmail(tenant.getAdminEmail());
        response.setCode(tenant.getCode());
        response.setAddress(tenant.getAddress());
        response.setCompanyName(tenant.getCompanyName());
        response.setPhone(tenant.getPhone());

        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = httpRequest.getHeader("Authorization");

            if (token != null && !token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = licenseServiceUrl + "/by-tenant/{tenantId}";
            ResponseEntity<List> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class,
                    tenant.getId()
            );
            List<String> licenseKeys = responseEntity.getBody();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des licences du tenant " + tenant.getId());
            e.printStackTrace();
            response.setLicenseKeys(List.of()); // Valeur vide en cas d'erreur
        }

        return response;
    }





}
