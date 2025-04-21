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
@RequestMapping("/api/licence-definitions")
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

    public LicenceDefinition create(@RequestBody LicenceDefinition def) {

        return service.create(def);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public LicenceDefinition update(@PathVariable UUID id, @RequestBody LicenceDefinition def) {
        return service.update(id, def);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
