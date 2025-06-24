package com.example.tenant.Services;

import com.example.tenant.Clients.AuthServiceClient;
import com.example.tenant.Clients.LicenceServiceClient;
import com.example.tenant.Dto.*;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.SipProfileRepository;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SipUserService {

    @Autowired
    private SipProfileRepository sipProfileRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private LicenceServiceClient licenseServiceClient;

    @Autowired
    private EmailService emailService;

    @Transactional
    public SipUserCreationResponse createSipUser(CreateSipUserRequest request, String authToken) {
        // Vérifier si le tenant existe
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Vérifier les licences disponibles
        List<LicenceAssignmentRequest> licences = licenseServiceClient.getLicencesByTenant(authToken, request.getTenantId());

        LicenceAssignmentRequest matchingLicence = licences.stream()
                .filter(l -> l.getLicenceDefinitionId().equals(request.getLicenceDefinitionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Licence not assigned to this tenant"));

        if (matchingLicence.getUsedUsers() >= matchingLicence.getMaxUsers()) {
            throw new RuntimeException("Licence quota exceeded");
        }

        // Créer le SIP Profile
        SipProfile sipProfile = new SipProfile();
        sipProfile.setUsername(request.getUsername());
        sipProfile.setEmail(request.getEmail());
        String password = UUID.randomUUID().toString().substring(0, 10);
        sipProfile.setPassword(password);
        sipProfile.setExtension(request.getExtension());
        sipProfile.setTenantId(request.getTenantId());
        sipProfile.setDomainName(tenant.getDomainName());
        sipProfile.setLicenceDefinitionId(request.getLicenceDefinitionId());
        sipProfile.setActive(true);

        SipProfile savedProfile = sipProfileRepository.save(sipProfile);

        try {
            // Enregistrer l'utilisateur dans le service d'authentification
            RegisterUserRequest userRequest = new RegisterUserRequest(
                    request.getEmail(),
                    password,
                    "USER", // ou "ADMIN_TENANT" selon vos besoins
                    request.getTenantId()
            );

            authServiceClient.registerSipUser(authToken, userRequest);

            // Envoi des credentials
            emailService.sendCredentials(request.getEmail(), password);
        } catch (Exception e) {
            // En cas d'échec, supprimer le profil SIP créé
            sipProfileRepository.delete(savedProfile);
            throw new RuntimeException("Failed to register user in auth service", e);
        }

        // Mettre à jour le quota de licence
        UpdateLicenceAssignmentRequest updateRequest = new UpdateLicenceAssignmentRequest();
        updateRequest.setLicenceDefinitionId(request.getLicenceDefinitionId());
        updateRequest.setUsedUsers(matchingLicence.getUsedUsers() + 1);

        licenseServiceClient.updateLicenceAssignment(request.getTenantId(), updateRequest, authToken);

        // Retourner la réponse DTO au lieu de l'entité
        return new SipUserCreationResponse(
                savedProfile.getId(),
                savedProfile.getUsername(),
                savedProfile.getEmail(),
                savedProfile.getExtension(),

                savedProfile.getDomainName(),

                savedProfile.isActive(),
                savedProfile.getPassword(),
                "SIP User created successfully"
        );
    }

    public List<SipProfile> getByTenant(Tenant tenant) {
        return sipProfileRepository.findAll()
                .stream().filter(u -> u.getTenantId().equals(tenant))
                .collect(Collectors.toList());
    }

    public List<SipProfile> getUsersByTenantId(Long tenantId) {
        return sipProfileRepository.findByTenantId(tenantId);
    }

   /* public Optional<SipProfile> findByUsernameAndTenant(String username, Long tenantId) {
        return sipProfileRepository.findByUsernameAndTenantId(username, tenantId);
    }*/
}