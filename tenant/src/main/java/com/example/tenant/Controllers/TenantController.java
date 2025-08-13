package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TrunkPoolService;
import com.example.tenant.Services.TrunkService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
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

    @Autowired
    private DidNumberService didNumberService;

    @Autowired
    private TrunkPoolService trunkPoolService;



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

    /********************** Trunks ***********************************************/

    @PostMapping("/trunks/{tenantId}")
    public Trunk createTrunk(@PathVariable Long tenantId, @RequestBody Trunk trunk) {
        return trunkService.createTrunk(trunk, tenantId);
    }

    @GetMapping("/trunks/getall/{tenantId}")
    public List<Trunk> getByTenant(@PathVariable Long tenantId) {
        return trunkService.getTrunksByTenant(tenantId);
    }

    @DeleteMapping("/trunks/{id}")
    public void delete(@PathVariable Long id) {
        trunkService.deleteTrunk(id);
    }


    /********************** DID ***********************************************/

    /** 🔹 Créer un DID */
    @PostMapping("/did/{tenantId}")
    public ResponseEntity<?> createDid(
            @PathVariable Long tenantId,
            @RequestBody DidNumber didNumber,
            @RequestHeader("role") String role
    ) {
        if (!"SUPER_ADMIN".equals(role) && !"ADMIN_TENANT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous n'avez pas la permission de créer un DID"));
        }

        try {
            // Associer le tenant
            didNumber.getTenant().setId(tenantId);
            DidNumber saved = didNumberService.save(didNumber);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** 🔹 Liste des DIDs actifs d'un tenant */
    @GetMapping("/did/getByTenant/{tenantId}")
    public ResponseEntity<List<DidNumber>> getDidByTenant(@PathVariable Long tenantId) {
        List<DidNumber> list = didNumberService.getByTenant(tenantId);
        return ResponseEntity.ok(list);
    }

    /** 🔹 Récupérer un DID par ID */
    @GetMapping("/did/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<DidNumber> did = didNumberService.getById(id);
        return did.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "DID introuvable")));
    }

    /** 🔹 Supprimer un DID */
    @DeleteMapping("/deleteDid/{id}")
    public ResponseEntity<?> deleteDid(
            @PathVariable Long id,
            @RequestHeader("role") String role
    ) {
        if (!"SUPER_ADMIN".equals(role) && !"ADMIN_TENANT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous n'avez pas la permission de supprimer un DID"));
        }

        didNumberService.delete(id);
        return ResponseEntity.ok(Map.of("message", "DID supprimé avec succès"));
    }

    /** 🔹 Activer/Désactiver un DID */
    @PutMapping("/did/{id}/active")
    public ResponseEntity<?> setActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestHeader("role") String role
    ) {
        if (!"SUPER_ADMIN".equals(role) && !"ADMIN_TENANT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous n'avez pas la permission de modifier l'état d'un DID"));
        }

        try {
            DidNumber updated = didNumberService.setActive(id, active);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{tenantId}/trunk-pool")
    public ResponseEntity<TrunkPool> createTrunkPool(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> payload
    ) {
        Long trunkId = Long.valueOf(payload.get("trunkId").toString());
        String countryCode = payload.get("countryCode").toString();
        String areaCode = payload.get("areaCode").toString();
        String localCode = payload.get("localCode").toString();
        int startNumber = Integer.parseInt(payload.get("startNumber").toString());
        int endNumber = Integer.parseInt(payload.get("endNumber").toString());

        TrunkPool pool = trunkPoolService.createPoolForTenant(
                tenantId, trunkId, countryCode, areaCode, localCode, startNumber, endNumber
        );

        return ResponseEntity.ok(pool);
    }

    @GetMapping("/trunk_pool/{tenantId}")
    public List<TrunkPool> getPoolsByTenant(@PathVariable Long tenantId) {
        return trunkPoolService.getActivePoolsByTenant(tenantId);
    }
}