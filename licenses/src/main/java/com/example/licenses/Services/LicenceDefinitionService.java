package com.example.licenses.Services;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Entities.LicenseStatus;
import com.example.licenses.Entities.TenantLicense;
import com.example.licenses.Repositories.LicenceDefinitionRepository;
import com.example.licenses.Repositories.LicenseRepository;
import com.example.licenses.dto.AssignLicenseRequest;
import com.example.licenses.dto.LicenseStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service// Génère un constructeur avec tous les champs 'final'
public class LicenceDefinitionService {

    @Autowired
    private LicenceDefinitionRepository repository;

    @Autowired
    private LicenseRepository licenseRepo;

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

    // Affecter une licence à un tenant
    public void assignLicense(AssignLicenseRequest request) {
        TenantLicense license = new TenantLicense();
        license.setTenantId(request.getTenantId());
        license.setLicenseKey(request.getLicenseKey());
        license.setStartDate(request.getStartDate());
        license.setEndDate(request.getEndDate());
        license.setMaxUsers(request.getMaxUsers());
        license.setUsedUsers(0);
        license.setStatus(calculateStatus(request.getStartDate(), request.getEndDate()));
        licenseRepo.save(license);
    }

    // Obtenir le statut de la licence d’un tenant
    public LicenseStatusResponse getLicenseStatus(UUID tenantId) {
        return licenseRepo.findByTenantId(tenantId)
                .map(l -> new LicenseStatusResponse(l.getTenantId(), l.getStatus()))
                .orElseThrow(() -> new RuntimeException("License not found"));
    }

    // Renouveler la licence
    public void renewLicense(UUID tenantId, LocalDate newEndDate) {
        TenantLicense license = licenseRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("License not found"));
        license.setEndDate(newEndDate);
        license.setStatus(calculateStatus(license.getStartDate(), newEndDate));
        licenseRepo.save(license);
    }

    // Vérifier et mettre à jour les statuts des licences
    public void checkAndUpdateStatuses() {
        for (TenantLicense license : licenseRepo.findAll()) {
            LicenseStatus newStatus = calculateStatus(license.getStartDate(), license.getEndDate());
            if (license.getStatus() != newStatus) {
                license.setStatus(newStatus);
                licenseRepo.save(license);
            }
        }
    }

    // Calcul du statut actuel
    private LicenseStatus calculateStatus(LocalDate start, LocalDate end) {
        LocalDate now = LocalDate.now();
        if (now.isBefore(start)) return LicenseStatus.PENDING;
        if (now.isAfter(end)) return LicenseStatus.EXPIRED;
        return LicenseStatus.ACTIVE;
    }
}