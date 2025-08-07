package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TrunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SipUserService userSipService;
    @Autowired
    private TrunkService trunkService;


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

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        Optional<Tenant> tenant = tenantRepository.findById(id);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{tenantId}/deactivate")
    public ResponseEntity<?> deactivateTenant(
            @PathVariable Long tenantId,
            @RequestHeader("Authorization") String token,
            @RequestHeader("role") String role) {

        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to deactivate tenants"));
        }

        try {
            tenantService.deactivateTenant(tenantId, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{tenantId}/activate")
    public ResponseEntity<?> activateTenant(
            @PathVariable Long tenantId,
            @RequestHeader("Authorization") String token,
            @RequestHeader("role") String role) {

        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to deactivate tenants"));
        }

        try {
            tenantService.activateTenant(tenantId, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/search")
    public List<Tenant> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String context,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer prefix,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) String timezone
    ) {
        return tenantService.searchTenants(name, context, code, prefix, createdAfter, timezone);
    }



    // ✅ Total d'utilisateurs disponibles selon les licences
    @GetMapping("/available-users")
    public ResponseEntity<Integer> getTotalAvailableUsers(@RequestHeader("Authorization") String token) {
        int total = tenantService.getTotalAvailableUsers(token);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllTenantNames() {
        List<String> tenantNames = tenantService.getAllTenantNames();
        return ResponseEntity.ok(tenantNames);
    }
    @GetMapping("/total-used-users")
    public ResponseEntity<Integer> getTotalUsedUsers(@RequestHeader("Authorization") String authToken) {
        try {
            int totalUsedUsers = tenantService.getTotalUsedUsers(authToken);
            return ResponseEntity.ok(totalUsedUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/trunks/tenant/{tenantId}")
    public Trunk createTrunk(@PathVariable Long tenantId, @RequestBody Trunk trunk) {
        return trunkService.createTrunk(trunk, tenantId);
    }

    @GetMapping("/trunks/tenant/{tenantId}")
    public List<Trunk> getByTenant(@PathVariable Long tenantId) {
        return trunkService.getTrunksByTenant(tenantId);
    }

    @DeleteMapping("/trunks/{id}")
    public void delete(@PathVariable Long id) {
        trunkService.deleteTrunk(id);
    }



}