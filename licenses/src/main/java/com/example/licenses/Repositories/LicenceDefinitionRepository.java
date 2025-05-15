package com.example.licenses.Repositories;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Entities.LicenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenceDefinitionRepository extends JpaRepository<LicenceDefinition, UUID> {
    // Méthode pour vérifier si une licence avec un type donné existe déjà
    boolean existsByType(LicenceType type);
    Optional<LicenceDefinition> findByKey(String key);
}