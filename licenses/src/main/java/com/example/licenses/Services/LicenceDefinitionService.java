package com.example.licenses.Services;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Entities.LicenceType;
import com.example.licenses.Entities.LicenceAssignment;
import com.example.licenses.Repositories.LicenceAssignmentRepository;
import com.example.licenses.Repositories.LicenceDefinitionRepository;

import com.example.licenses.dto.LicenceAssignmentRequest;
import com.example.licenses.dto.MultipleLicenceAssignmentRequest;
import com.example.licenses.dto.UpdateLicenceAssignmentRequest;
import com.example.licenses.dto.UpdateLicenceAssignmentsRequest;
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
            assignment.setStartDate(LocalDate.now());
            assignment.setEndDate(LocalDate.now().plusYears(1));
            assignment.setActive(true);
            savedLicences.add(repositoryLicence.save(assignment));
        }
        return savedLicences;
    }

    public void checkExpiredLicences() {
        List<LicenceAssignment> licences = repositoryLicence.findAll();
        for (LicenceAssignment licence : licences) {
            if (licence.getEndDate().isBefore(LocalDate.now()) && licence.isActive()) {
                licence.setActive(false);
                repositoryLicence.save(licence);
            }
        }
    }

    public List<LicenceAssignment> getLicencesByTenantId(Long tenantId) {
        return repositoryLicence.findByTenantId(tenantId);
    }
    public void deleteLicencesByTenantId(Long tenantId) {
        List<LicenceAssignment> licences = repositoryLicence.findByTenantId(tenantId);
        repositoryLicence.deleteAll(licences);
    }
    public List<LicenceAssignment> updateLicenceAssignments(UpdateLicenceAssignmentsRequest request) {
        List<LicenceAssignment> updatedLicences = new ArrayList<>();

        for (UpdateLicenceAssignmentRequest updateRequest : request.getLicences()) {
            // Trouver l'affectation existante pour ce tenant et cette licence
            LicenceAssignment assignment = repositoryLicence.findByTenantIdAndLicenceDefinitionId(
                    request.getTenantId(),
                    updateRequest.getLicenceDefinitionId()
            ).orElseThrow(() -> new RuntimeException(
                    "Affectation non trouvée pour tenantId: " + request.getTenantId() +
                            " et licenceDefinitionId: " + updateRequest.getLicenceDefinitionId()
            ));

            // Mettre à jour les champs si présents dans la requête
            if (updateRequest.getMaxUsers() != null) {
                assignment.setMaxUsers(updateRequest.getMaxUsers());
            }

            if (Boolean.TRUE.equals(updateRequest.getRenew())) {
                // Renouvellement: prolonger d'un an à partir de maintenant
                assignment.setEndDate(LocalDate.now().plusYears(1));
            }

            // Réactiver la licence si elle était expirée
            assignment.setActive(true);

            updatedLicences.add(repositoryLicence.save(assignment));
        }

        return updatedLicences;
    }

    public Optional<LicenceAssignment> findByTenantIdAndLicenceDefinitionId(Long tenantId, UUID licenceDefinitionId) {
        return repositoryLicence.findByTenantIdAndLicenceDefinitionId(tenantId, licenceDefinitionId);
    }
}