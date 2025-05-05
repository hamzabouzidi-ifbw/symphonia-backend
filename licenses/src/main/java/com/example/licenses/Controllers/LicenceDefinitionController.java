package com.example.licenses.Controllers;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Services.LicenceDefinitionService;
import com.example.licenses.dto.LicenceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("licenses")
public class LicenceDefinitionController {

    @Autowired
    private LicenceDefinitionService service;



    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addLicense(@RequestBody LicenceDefinition def,
                                        @RequestHeader("role") String role) {

        if ("SUPER_ADMIN".equals(role)) {

            LicenceDefinition createdLicence = service.create(def);

            LicenceDTO response = new LicenceDTO(

                    createdLicence.getId(),
                    createdLicence
            );

            return ResponseEntity.status(201).body(response);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to create a licence."));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<LicenceDefinition> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public LicenceDefinition getById(@PathVariable UUID id) {
        return service.findById(id);
    }




    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public LicenceDefinition update(@PathVariable UUID id, @RequestBody LicenceDefinition def) {
        return service.update(id, def);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}