package com.example.tenant.Controllers;

import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody CreateTenantRequest request,@RequestHeader("role") String role) {

        if ("SUPER_ADMIN".equals(role)) {
            try {
                tenantService.createTenant(request);
                return ResponseEntity.ok()
                        .body(new ApiResponse(true, "Tenant et admin créés avec succès."));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(false, e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Une erreur interne est survenue."));
            }
        } else {
            // Si l'utilisateur n'a pas les permissions appropriées
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to create a tenant."));
        }

    }

    // Classe interne pour standardiser les réponses
    private static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}