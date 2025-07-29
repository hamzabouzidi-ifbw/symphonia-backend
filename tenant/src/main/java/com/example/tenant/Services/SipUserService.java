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
    @Autowired
    private ExtensionService extensionService;


    @Transactional
    public SipProfile createSipUser(CreateSipUserRequest request) {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = httpRequest.getHeader("Authorization");

        Boolean emailExistsLocal = sipProfileRepository.existsByEmail(request.getEmail());
        Boolean emailExistsRemote = authServiceClient.checkIfUserExists(request.getEmail(), token);

        if (Boolean.TRUE.equals(emailExistsLocal) || Boolean.TRUE.equals(emailExistsRemote)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));


        // 1. Vérification email déjà existant dans auth-service
        Boolean emailExists = authServiceClient.checkIfUserExists(request.getEmail(), token);
        if (Boolean.TRUE.equals(emailExists)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // 2. Vérification licence
        List<LicenceAssignmentRequest> licences = licenceServiceClient.getLicencesByTenant(token, request.getTenantId());

        LicenceAssignmentRequest matchingLicence = licences.stream()
                .filter(l -> l.getLicenceDefinitionId().equals(request.getLicenceDefinitionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Licence non assignée au tenant"));

        if (matchingLicence.getUsedUsers() >= matchingLicence.getMaxUsers()) {
            throw new RuntimeException("Quota de licence dépassé");
        }
        String extension;
        if (request.getExtension() != null && !request.getExtension().isEmpty()) {
            // Vérifier si l'extension spécifiée est valide pour ce tenant
            if (!extensionService.isExtensionValidForTenant(request.getTenantId(), request.getExtension())) {
                throw new RuntimeException("Extension invalide pour ce tenant");
            }
            if (sipProfileRepository.existsByExtension(request.getExtension())) {
                throw new RuntimeException("Extension déjà utilisée");
            }
            extension = request.getExtension();
        } else {
            // Générer automatiquement la prochaine extension disponible
            extension = extensionService.generateNextExtension(request.getTenantId());
        }
        // 3. Création SIP
        String password = UUID.randomUUID().toString().substring(0, 10);
        SipProfile sipProfile = new SipProfile();
        sipProfile.setUsername(request.getUsername());
        sipProfile.setEmail(request.getEmail());
        sipProfile.setPassword(password);
        sipProfile.setExtension(extension);
        sipProfile.setTenantId(request.getTenantId());
        sipProfile.setDomainName(tenant.getDomainName());
        sipProfile.setLicenceDefinitionId(request.getLicenceDefinitionId());
        sipProfile.setActive(true);

        SipProfile savedProfile = sipProfileRepository.save(sipProfile);

        // 4. Mise à jour licence
        UpdateLicenceAssignmentRequest updateRequest = new UpdateLicenceAssignmentRequest();
        updateRequest.setLicenceDefinitionId(request.getLicenceDefinitionId());
        updateRequest.setUsedUsers(matchingLicence.getUsedUsers() + 1);

        licenceServiceClient.updateLicenceAssignment(request.getTenantId(), updateRequest, token);

        try {
            RegisterUserRequest userRequest = new RegisterUserRequest(
                    request.getEmail(),
                    password,
                    "USER",
                    request.getTenantId()
            );

            authServiceClient.registerSipUser(token, userRequest);

            emailService.sendCredentials(request.getEmail(), password);
        } catch (Exception e) {
            sipProfileRepository.delete(savedProfile);
            throw new RuntimeException("Échec lors de l'enregistrement dans le service d'auth", e);
        }

        return savedProfile;
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


    public Optional<SipProfile> findByDomainAndExtension(String domain, String extension) {
        return sipProfileRepository.findByDomainNameAndExtensionAndActiveTrue(domain, extension);
    }

    public List<SipProfile> getAllSipProfiles() {
        return sipProfileRepository.findAll();
    }




}