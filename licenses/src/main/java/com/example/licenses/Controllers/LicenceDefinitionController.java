package com.example.licenses.Controllers;

import com.example.licenses.Entities.LicenceDefinition;
import com.example.licenses.Entities.LicenceType;
import com.example.licenses.Entities.LicenceAssignment;
import com.example.licenses.Services.LicenceDefinitionService;

import com.example.licenses.dto.*;
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



    @PostMapping("/assign-multiple")
    public ResponseEntity<List<LicenceAssignment>> assignMultiple(@RequestBody MultipleLicenceAssignmentRequest request) {
        System.out.println("===== Licence assign endpoint called =====");

        System.out.println("TenantId: " + request.getTenantId());
        System.out.println("Licences:");
        if (request.getLicences() != null) {
            request.getLicences().forEach(licence -> {
                System.out.println(" - licenceDefinitionId: " + licence.getLicenceDefinitionId());
                System.out.println("   tenantId: " + licence.getTenantId());
                System.out.println("   maxUsers: " + licence.getMaxUsers());
            });
        } else {
            System.out.println("No licences provided.");
        }

        return ResponseEntity.ok(service.assignMultipleLicences(request));
    }


    // Ajouter une licence
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addLicense(@RequestBody LicenceDefinition def,
                                        @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            try {
                // Appel au service pour créer la licence
                LicenceDefinition createdLicence = service.create(def);

                // Création du DTO pour la réponse
                LicenceDTO response = new LicenceDTO(
                        createdLicence.getId(),
                        createdLicence
                );

                // Retourner la réponse avec la licence créée
                return ResponseEntity.status(201).body(response);
            } catch (IllegalArgumentException e) {
                // Gérer les erreurs de validation
                return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
            }
        } else {
            // Si l'utilisateur n'a pas les permissions appropriées
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to create a licence."));
        }
    }

    // Récupérer toutes les licences
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<LicenceDefinition> getAll() {
        return service.findAll();
    }

    // Récupérer une licence par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public LicenceDefinition getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    // Mettre à jour une licence
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public LicenceDefinition update(@PathVariable UUID id, @RequestBody LicenceDefinition def) {
        return service.update(id, def);
    }

    // Supprimer une licence
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

}