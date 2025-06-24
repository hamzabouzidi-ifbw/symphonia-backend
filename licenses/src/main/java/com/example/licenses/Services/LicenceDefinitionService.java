package com.example.licenses.Services;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Entities.LicenceAssignment;
import com.example.licenses.Repositories.LicenceAssignmentRepository;
import com.example.licenses.Repositories.LicenceDefinitionRepository;

import com.example.licenses.dto.LicenceAssignmentRequest;
import com.example.licenses.dto.MultipleLicenceAssignmentRequest;
import com.example.licenses.dto.UpdateLicenceAssignmentRequest;
import com.example.licenses.dto.UpdateLicenceAssignmentUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service// Génère un constructeur avec tous les champs 'final'
public class LicenceDefinitionService {

    @Autowired
    private LicenceDefinitionRepository repository;

    @Autowired
    private LicenceAssignmentRepository repositoryLicence;

    // Récupérer toutes les licences
    public List<LicenceDefinition> findAll() {
        return repository.findAll();
    }

    // Récupérer une licence par son ID
    public LicenceDefinition findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licence not found"));
    }

    // Créer une licence
    public LicenceDefinition create(LicenceDefinition def) {
        if (repository.existsByType(def.getType())) {
            throw new IllegalArgumentException("Une licence de ce type existe déjà.");
        }
        def.setKey(generateLicenseKey());
        return repository.save(def);
    }

    // Mettre à jour une licence
    public LicenceDefinition update(UUID id, LicenceDefinition def) {
        LicenceDefinition existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licence not found"));
        existing.setType(def.getType());
        existing.setFeatures(def.getFeatures());
        return repository.save(existing);
    }

    // Supprimer une licence
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    // Générer une clé de licence unique
    private String generateLicenseKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    public List<LicenceAssignment> assignMultipleLicences(MultipleLicenceAssignmentRequest request) {
        List<LicenceAssignment> savedLicences = new ArrayList<>();
        for (LicenceAssignmentRequest licenceReq : request.getLicences()) {
            LicenceAssignment assignment = new LicenceAssignment();
            assignment.setTenantId(request.getTenantId());
            assignment.setLicenceDefinitionId(licenceReq.getLicenceDefinitionId());
            assignment.setMaxUsers(licenceReq.getMaxUsers());
            assignment.setUsedUsers(0);

            savedLicences.add(repositoryLicence.save(assignment));
        }
        return savedLicences;
    }



    public List<LicenceAssignment> getLicencesByTenantId(Long tenantId) {
        return repositoryLicence.findByTenantId(tenantId);
    }



    public void deleteByTenantId(Long tenantId) {
        List<LicenceAssignment> licences = repositoryLicence.findByTenantId(tenantId);
        if (licences.isEmpty()) {
            throw new RuntimeException("Aucune licence trouvée pour ce tenant");
        }
        repositoryLicence.deleteAll(licences);
    }

    public LicenceAssignment updateSingleLicenceAssignment(Long tenantId, UpdateLicenceAssignmentRequest updateRequest) {
        Optional<LicenceAssignment> existingAssignment = repositoryLicence.findByTenantIdAndLicenceDefinitionId(
                tenantId,
                updateRequest.getLicenceDefinitionId()
        );

        LicenceAssignment assignment;

        if (existingAssignment.isPresent()) {
            assignment = existingAssignment.get();

            // Mise à jour des champs selon la requête
            if (updateRequest.getMaxUsers() != null) {
                assignment.setMaxUsers(updateRequest.getMaxUsers());
            }

            // Exemple si tu veux réactiver la licence
            //assignment.setActive(true);

        } else {
            // Si non trouvé, tu peux lever une exception ou créer une nouvelle affectation
            throw new RuntimeException("Licence non trouvée pour ce tenant");
        }

        return repositoryLicence.save(assignment);
    }

    public Optional<LicenceAssignment> findByTenantIdAndLicenceDefinitionId(Long tenantId, UUID licenceDefinitionId) {
        return repositoryLicence.findByTenantIdAndLicenceDefinitionId(tenantId, licenceDefinitionId);
    }
    public LicenceAssignment assignSingleLicence(LicenceAssignmentRequest request) {
        // Vérifier si le tenant a déjà ce type de licence
        Optional<LicenceAssignment> existingAssignment = repositoryLicence.findByTenantIdAndLicenceDefinitionId(
                request.getTenantId(),
                request.getLicenceDefinitionId()
        );

        if (existingAssignment.isPresent()) {
            throw new IllegalArgumentException("Ce tenant a déjà une licence de ce type");
        }

        // Créer une nouvelle affectation de licence
        LicenceAssignment assignment = new LicenceAssignment();
        assignment.setTenantId(request.getTenantId());
        assignment.setLicenceDefinitionId(request.getLicenceDefinitionId());
        assignment.setMaxUsers(request.getMaxUsers());
        assignment.setUsedUsers(0); // Initialiser à 0
        // Date d'affectation actuelle

        return repositoryLicence.save(assignment);
    }
    public void deleteSingleLicenceAssignment(Long tenantId, UUID licenceDefinitionId) {
        Optional<LicenceAssignment> assignment = repositoryLicence.findByTenantIdAndLicenceDefinitionId(tenantId, licenceDefinitionId);

        if (assignment.isEmpty()) {
            throw new RuntimeException("Affectation de licence non trouvée pour ce tenant");
        }

        repositoryLicence.delete(assignment.get());
    }

    public LicenceAssignment updateSingleLicenceAssignmentUser(Long tenantId, UpdateLicenceAssignmentUserRequest request) {
        LicenceAssignment assignment = repositoryLicence
                .findByTenantIdAndLicenceDefinitionId(tenantId, request.getLicenceDefinitionId())
                .orElseThrow(() -> new RuntimeException("Licence assignment not found"));

        assignment.setUsedUsers(request.getUsedUsers());
        return repositoryLicence.save(assignment);
    }
}