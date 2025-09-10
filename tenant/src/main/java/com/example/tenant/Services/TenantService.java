package com.example.tenant.Services;

import com.example.tenant.Clients.AuthServiceClient;
import com.example.tenant.Clients.LicenceServiceClient;
import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.TenantRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import java.io.*;
import java.util.stream.Collectors;

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
        String domainName = request.getTenantName() + ".symphonia.ifbw.net";
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
        notifyFreeSwitchUpdate(updatedTenant.getDomainName());

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
            notifyFreeSwitchUpdate(tenant.getDomainName());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du tenant: " + e.getMessage());
        }
    }

    public Optional<Tenant> getByDomain(String domain) {
        return tenantRepository.findByDomainName(domain);
    }

    public Optional<Tenant> getTenantById(Long id) {
        return tenantRepository.findById(id);
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

    //freeswitch
    private void notifyFreeSwitchUpdate(String domain) {
        try {
            Runtime.getRuntime().exec("fs_cli -x 'reloadxml'");
            System.out.println("FreeSWITCH XML reload triggered for domain: " );
        } catch (IOException e) {
            System.out.println("Failed to reload FreeSWITCH XML");
        }
    }

    public List<Tenant> searchTenants(String name, String context, String code, Integer prefix, LocalDateTime createdAfter, String timezone) {
        Specification<Tenant> spec = Specification.where(TenantSpecification.tenantNameContains(name))
                .and(TenantSpecification.contextNameContains(context))
                .and(TenantSpecification.codeContains(code))
                .and(TenantSpecification.hasPrefix(prefix))
                .and(TenantSpecification.createdAfter(createdAfter))
                .and(TenantSpecification.hasTimezone(timezone));

        return tenantRepository.findAll(spec);
    }


    public int getTotalAvailableUsers(String token) {
        List<Tenant> tenants = tenantRepository.findAll();
        int totalAvailableUsers = 0;

        for (Tenant tenant : tenants) {
            try {
                List<LicenceAssignmentRequest> licences = licenceServiceClient.getLicencesByTenant(token, tenant.getId());
                for (LicenceAssignmentRequest licence : licences) {
                    int available = licence.getMaxUsers() - licence.getUsedUsers();
                    totalAvailableUsers += Math.max(available, 0); // On évite les valeurs négatives
                }
            } catch (Exception e) {
                // Log, ignorer ou relancer selon votre stratégie
                System.err.println("Erreur lors de la récupération des licences pour le tenant " + tenant.getId());
            }
        }

        return totalAvailableUsers;
    }


    public List<String> getAllTenantNames() {
        return tenantRepository.findAll().stream()
                .map(Tenant::getTenantName)
                .collect(Collectors.toList());
    }

    public int getTotalUsedUsers(String token) {
        List<Tenant> tenants = tenantRepository.findAll();
        int totalUsedUsers = 0;

        for (Tenant tenant : tenants) {
            try {
                List<LicenceAssignmentRequest> licences = licenceServiceClient.getLicencesByTenant(token, tenant.getId());
                for (LicenceAssignmentRequest licence : licences) {
                    totalUsedUsers += licence.getUsedUsers();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des licences pour le tenant " + tenant.getId() + ": " + e.getMessage());
            }
        }

        return totalUsedUsers;
    }

    public List<LicenceAssignmentRequest> getAllLicenceAssignments(String token) {
        List<Tenant> tenants = tenantRepository.findAll();
        List<LicenceAssignmentRequest> allAssignments = new ArrayList<>();

        for (Tenant tenant : tenants) {
            try {
                List<LicenceAssignmentRequest> licences =
                        licenceServiceClient.getLicencesByTenant(token, tenant.getId());
                allAssignments.addAll(licences);
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des licences pour le tenant "
                        + tenant.getId() + ": " + e.getMessage());
            }
        }
        return allAssignments;
    }





}