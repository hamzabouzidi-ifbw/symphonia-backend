package com.example.licenses.Services;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Repositories.LicenceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LicenceDefinitionService {
    @Autowired
    private LicenceDefinitionRepository repository;

    public List<LicenceDefinition> findAll() {
        return repository.findAll();
    }

    public LicenceDefinition findById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public LicenceDefinition create(LicenceDefinition def) {
        return repository.save(def);
    }

    public LicenceDefinition update(UUID id, LicenceDefinition def) {
        LicenceDefinition existing = repository.findById(id).orElseThrow();
        existing.setType(def.getType());
        existing.setFeatures(def.getFeatures());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}