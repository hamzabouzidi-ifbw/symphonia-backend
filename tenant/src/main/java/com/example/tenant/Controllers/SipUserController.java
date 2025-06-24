package com.example.tenant.Controllers;

import com.example.tenant.Dto.CreateSipUserRequest;
import com.example.tenant.Dto.SipUserCreationResponse;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Services.SipUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/sip-users")

public class SipUserController {
    @Autowired
    private SipUserService sipUserService;

    @PostMapping
    public ResponseEntity<SipUserCreationResponse> createSipUser(
            @RequestBody CreateSipUserRequest request,
            @RequestHeader("Authorization") String token) {

        SipUserCreationResponse response = sipUserService.createSipUser(request, token);
        return ResponseEntity.ok(response);
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





}