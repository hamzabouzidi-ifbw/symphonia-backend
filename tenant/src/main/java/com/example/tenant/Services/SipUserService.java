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
    private LicenceServiceClient licenceServiceClient;

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

    @Transactional
    public SipUserCreationResponse updateSipUser(Long sipUserId, CreateSipUserRequest request) {
        SipProfile existingUser = sipProfileRepository.findById(sipUserId)
                .orElseThrow(() -> new RuntimeException("SIP User not found"));

        // Vérification du username
        Optional<SipProfile> userWithSameUsername = sipProfileRepository.findByUsername(request.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(sipUserId)) {
            throw new RuntimeException("Un autre utilisateur avec ce nom existe déjà");
        }

        // Vérification de l'email
        Optional<SipProfile> userWithSameEmail = sipProfileRepository.findByEmail(request.getEmail());
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(sipUserId)) {
            throw new RuntimeException("Un autre utilisateur avec cet email existe déjà");
        }

        // Vérification de l'extension
        if (request.getExtension() != null) {
            Optional<SipProfile> userWithSameExtension = sipProfileRepository.findByExtension(request.getExtension());
            if (userWithSameExtension.isPresent() && !userWithSameExtension.get().getId().equals(sipUserId)) {
                throw new RuntimeException("Un autre utilisateur avec cette extension existe déjà");
            }
        }

        // Mise à jour des champs sauf le mot de passe
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setExtension(request.getExtension());
        existingUser.setLicenceDefinitionId(request.getLicenceDefinitionId());

        SipProfile updatedUser = sipProfileRepository.save(existingUser);

        return new SipUserCreationResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getExtension(),
                updatedUser.getDomainName(),
                updatedUser.isActive(),
                null, // mot de passe non retourné ou null
                "SIP User updated successfully"
        );
    }


    @Transactional
    public void deleteSipUser(Long sipUserId, String authToken) {
        // 1. Récupérer le SIP Profile
        SipProfile user = sipProfileRepository.findById(sipUserId)
                .orElseThrow(() -> new RuntimeException("SIP User not found"));

        // 2. Récupérer la licence affectée à ce SIP user
        List<LicenceAssignmentRequest> licences = licenseServiceClient.getLicencesByTenant(authToken, user.getTenantId());

        LicenceAssignmentRequest licence = licences.stream()
                .filter(l -> l.getLicenceDefinitionId().equals(user.getLicenceDefinitionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Licence non trouvée pour cet utilisateur"));

        // 3. Décrémenter le nombre d'utilisateurs utilisés
        UpdateLicenceAssignmentRequest updateRequest = new UpdateLicenceAssignmentRequest();
        updateRequest.setLicenceDefinitionId(user.getLicenceDefinitionId());
        updateRequest.setUsedUsers(Math.max(licence.getUsedUsers() - 1, 0)); // pour éviter -1

        licenseServiceClient.updateLicenceAssignment(user.getTenantId(), updateRequest, authToken);

        // 4. Supprimer le SIP user de la base
        sipProfileRepository.delete(user);
    }


    public List<SipProfile> getSipUsersByTenantId(Long tenantId) {
        return sipProfileRepository.findAllByTenantId(tenantId);
    }

    public Optional<SipProfile> getSipUserById(Long id) {
        return sipProfileRepository.findById(id);
    }

    public List<SipProfile> getByTenant(Tenant tenant) {
        return sipProfileRepository.findAll()
                .stream().filter(u -> u.getTenantId().equals(tenant))
                .collect(Collectors.toList());
    }

    public List<SipProfile> getUsersByTenantId(Long tenantId) {
        return sipProfileRepository.findByTenantId(tenantId);
    }


}