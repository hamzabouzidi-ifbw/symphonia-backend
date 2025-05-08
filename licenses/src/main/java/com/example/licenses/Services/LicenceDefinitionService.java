package com.example.licenses.Services;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Repositories.LicenceDefinitionRepository;
import com.example.licenses.Entities.LicenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LicenceDefinitionService {

    @Autowired
    private LicenceDefinitionRepository repository;

    // Récupérer toutes les licences
    public List<LicenceDefinition> findAll() {
        return repository.findAll();
    }

    // Récupérer une licence par son ID
    public LicenceDefinition findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Licence not found"));
    }

    // Créer une licence
    public LicenceDefinition create(LicenceDefinition def) {
        // Vérifier si une licence du même type existe déjà
        boolean licenceExists = repository.existsByType(def.getType());
        if (licenceExists) {
            throw new IllegalArgumentException("Une licence de ce type existe déjà.");
        }

        // Générer la clé de licence
        def.setKey(generateLicenseKey()); // Génération de la clé ici
        return repository.save(def); // Sauvegarder la licence
    }

    // Mettre à jour une licence
    public LicenceDefinition update(UUID id, LicenceDefinition def) {
        LicenceDefinition existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Licence not found"));
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
}