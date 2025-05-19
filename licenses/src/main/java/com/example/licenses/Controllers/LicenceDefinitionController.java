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
    public ResponseEntity<?> assignMultiple(@RequestBody MultipleLicenceAssignmentRequest request,
                                            @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
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
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to assign licences."));
        }
    }


    // Ajouter une licence
    @PostMapping("/add")
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


    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            return ResponseEntity.ok(service.findAll());
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to display licences."));
        }
    }


    // Récupérer une licence par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id, @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            LicenceDefinition licence = service.findById(id);
            return ResponseEntity.ok(licence);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to display license."));
        }
    }



    // Mettre à jour une licence
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestBody LicenceDefinition def,
                                    @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            LicenceDefinition updated = service.update(id, def);
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to update the license."));
        }
    }


    // Supprimer une licence
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id,
                                    @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to delete the license."));
        }
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<?> getLicencesByTenant(@PathVariable Long tenantId,
                                                 @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            List<LicenceAssignment> licences = service.getLicencesByTenantId(tenantId);
            return ResponseEntity.ok(licences);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to access licences."));
        }
    }
    @DeleteMapping("/by-tenant/{tenantId}")
    public ResponseEntity<?> deleteLicencesByTenant(@PathVariable Long tenantId,
                                                    @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            service.deleteLicencesByTenantId(tenantId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to delete licences."));
        }
    }
    @PutMapping("/assignments/update")
    public ResponseEntity<?> updateLicenceAssignments(@RequestBody UpdateLicenceAssignmentsRequest request,
                                                      @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            try {
                List<LicenceAssignment> updatedLicences = service.updateLicenceAssignments(request);
                return ResponseEntity.ok(updatedLicences);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to update licence assignments."));
        }
    }
}