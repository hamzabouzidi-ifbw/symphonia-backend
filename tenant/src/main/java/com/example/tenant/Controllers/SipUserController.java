package com.example.tenant.Controllers;

import com.example.tenant.Dto.CreateSipUserRequest;
import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Dto.SipUserCreationResponse;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/sip-users")

public class SipUserController {
    @Autowired
    private SipUserService sipUserService;
    @Autowired
    private DidNumberService didNumberService;

   @PostMapping()
   public ResponseEntity<?> createSipUser(
           @RequestBody CreateSipUserRequest request,
           @RequestHeader(value = "role", required = false) String role) {

       if (!"SUPER_ADMIN".equals(role)) {
           return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(Map.of("error", "You do not have permission to create a tenant."));
       }

       try {
           SipProfile profile = sipUserService.createSipUser(request);
           return ResponseEntity.ok(profile);
       } catch (RuntimeException e) {
           // Tu peux ici affiner le code selon le message ou type d'exception
           return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(Map.of("error", e.getMessage()));
       }
   }


    @PutMapping("/{id}")
    public ResponseEntity<SipUserCreationResponse> updateSipUser(
            @PathVariable Long id,
            @RequestBody CreateSipUserRequest request) {
        SipUserCreationResponse response = sipUserService.updateSipUser(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<SipProfile>> getSipUsersByTenantId(@PathVariable Long tenantId) {
        List<SipProfile> users = sipUserService.getSipUsersByTenantId(tenantId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SipProfile> getSipUserById(@PathVariable Long id) {
        return sipUserService.getSipUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSipUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            sipUserService.deleteSipUser(id, token);
            return ResponseEntity.ok("SIP User deleted and licence quota updated.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du SIP user: " + e.getMessage());
        }
    }


    @PostMapping("/dids")
    public ResponseEntity<DidNumber> createDid(@RequestBody DidNumber request) {
        return ResponseEntity.ok(didNumberService.createDid(request));
    }





}