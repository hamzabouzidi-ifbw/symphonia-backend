package com.example.licenses.Controllers;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Services.LicenceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("licenses")
public class LicenceDefinitionController {

    @Autowired
    private LicenceDefinitionService service;

    @GetMapping
    public List<LicenceDefinition> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public LicenceDefinition getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> create(@RequestBody LicenceDefinition def) {
        LicenceDefinition createdLicence = service.create(def);

        // Message de succès
        return ResponseEntity.status(201) // 201 Created
                .body("Licence created successfully with ID: " + createdLicence.getId());
    }


    @PutMapping("/{id}")
    public LicenceDefinition update(@PathVariable UUID id, @RequestBody LicenceDefinition def) {
        return service.update(id, def);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
