package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Entities.UsersConfig.DestinationType;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.DidNumberRepository;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkPoolRepository;
import com.example.tenant.Services.TenantService;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.TrunkPoolService;
import com.example.tenant.Services.TrunkService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

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
    private DidNumberService didNumberService;

    @Autowired
    private TrunkPoolService trunkPoolService;
    @Autowired
    private TrunkPoolRepository trunkPoolRepository;

    @Autowired
    private DidNumberRepository didNumberRepository;

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

    /********************** Trunks ***********************************************/

    @PostMapping("/add-pool/{tenantId}")
    public ResponseEntity<TrunkPool> createPool(
            @PathVariable Long tenantId,
            @RequestBody TrunkPoolCreateDTO dto) {

        TrunkPool pool = trunkPoolService.createTrunkPool(
                tenantId,
                dto.getCountryCode(),
                dto.getAreaCode(),
                dto.getLocalCode(),
                dto.getStartNumber(),
                dto.getEndNumber()
        );

        return ResponseEntity.ok(pool);
    }

    @PostMapping("/{poolId}/add-trunk")
    public ResponseEntity<Trunk> addTrunkToPool(
            @PathVariable Long poolId,
            @RequestBody TrunkCreateDTO dto) {

        Trunk trunk = trunkPoolService.createTrunkForPool(poolId, dto);
        return ResponseEntity.ok(trunk);
    }

    @PutMapping("/pool/{poolId}")
    public ResponseEntity<TrunkPool> updatePool(
            @PathVariable Long poolId,
            @RequestBody TrunkPoolCreateDTO dto) {
        return ResponseEntity.ok(trunkPoolService.updateTrunkPool(poolId, dto));
    }

    @PutMapping("/trunk/{trunkId}")
    public ResponseEntity<Trunk> updateTrunk(
            @PathVariable Long trunkId,
            @RequestBody TrunkCreateDTO dto) {
        return ResponseEntity.ok(trunkPoolService.updateTrunk(trunkId, dto));
    }
    // Afficher tous les TrunkPools
    @GetMapping("/pools")
    public ResponseEntity<List<TrunkPool>> getAllTrunkPools() {
        List<TrunkPool> pools = trunkPoolService.getAllTrunkPools();
        return ResponseEntity.ok(pools);
    }

    @GetMapping("/{poolId}/trunks")
    public ResponseEntity<List<Trunk>> getTrunksOfPool(@PathVariable Long poolId) {
        List<Trunk> trunks = trunkPoolService.getTrunksByTrunkPool(poolId);
        return ResponseEntity.ok(trunks);
    }

    @DeleteMapping("/delete-pool/{poolId}")
    public ResponseEntity<String> deleteTrunkPool(@PathVariable Long poolId) {
        trunkPoolService.deleteTrunkPool(poolId);
        return ResponseEntity.ok("TrunkPool et tous les Trunks associés ont été supprimés");
    }
    @DeleteMapping("/delete-trunk/{trunkId}")
    public ResponseEntity<String> deleteTrunk(@PathVariable Long trunkId) {
        trunkPoolService.deleteTrunk(trunkId);
        return ResponseEntity.ok("Trunk supprimé avec succès");
    }

    @GetMapping("/all-trunks")
    public List<Trunk> getAllTrunks() {
        return trunkPoolService.getAllTrunks();
    }

    @GetMapping("/{tenantId}/trunks-by-tenant")
    public List<Trunk> getTrunksByTenant(@PathVariable Long tenantId) {
        return trunkService.getTrunksByTenant(tenantId);
    }
    /********************** DID ***********************************************/

    /** 🔹 Créer un DID */
    @PostMapping("/create-did")
    public DidNumber createDid(@RequestBody DidRequest request) {
        return didNumberService.createDidForTenant(
                request.getTenantId(),
                request.getTrunkId(),
                request.getNumber(),
                request.getDestinationType(),
                request.getDestinationValue()
        );
    }

    // ------------------- UPDATE -------------------
    @PutMapping("/update-did/{didId}")
    public ResponseEntity<DidNumber> updateDid(
            @PathVariable Long didId,
            @RequestBody DidRequest request) {

        DidNumber did = didNumberService.updateDid(
                request.getTenantId(),
                didId,
                request.getTrunkId(),
                request.getNumber(),
                request.getDestinationType(),
                request.getDestinationValue()
        );
        return ResponseEntity.ok(did);
    }

    // ------------------- DELETE -------------------
    @DeleteMapping("/delete-did/{didId}")
    public ResponseEntity<String> deleteDid(@PathVariable Long didId) {
        didNumberService.deleteDid(didId);
        return ResponseEntity.ok("DID supprimé avec succès");
    }

    // ------------------- afficher dids -------------------


    @GetMapping("/dids") public ResponseEntity<List<DidNumber>> getAllDids() {
        List<DidNumber> dids = didNumberService.getAllDids();
        return ResponseEntity.ok(dids);
    }




}