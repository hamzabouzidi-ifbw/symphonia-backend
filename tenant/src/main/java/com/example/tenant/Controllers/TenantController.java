package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;


    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody CreateTenantRequest request,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            Tenant createdTenant = tenantService.createTenant(request);
            return ResponseEntity.ok(createdTenant);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to create a tenant."));
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllTenantsWithLicences(@RequestHeader("Authorization") String token,
                                                       @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            List<TenantWithLicencesResponse> tenants = tenantService.getAllTenantsWithLicences(token);
            return ResponseEntity.ok(tenants);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to access tenants."));
        }
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<?> updateTenant(@PathVariable Long tenantId,
                                          @RequestBody UpdateTenantRequest request,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role) || "ADMIN_TENANT".equals(role)) {
            try {
                Tenant updatedTenant = tenantService.updateTenant(tenantId, request);
                return ResponseEntity.ok(updatedTenant);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'avez pas la permission de modifier ce tenant."));
        }
    }
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<?> deleteTenant(@PathVariable Long tenantId,
                                          @RequestHeader("Authorization") String token,
                                          @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            try {
                tenantService.deleteTenant(tenantId, token);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'avez pas la permission de supprimer ce tenant."));
        }
    }
}