package com.example.tenant.Clients;

import com.example.tenant.Dto.RegisterUserRequest;
import com.example.tenant.Entities.SipProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "authentification", url = "${auth.service.url}")
public interface AuthServiceClient {

    @PostMapping("/authentification/register_tenant")
    void registerTenantUser(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterUserRequest userRequest
    );

    @DeleteMapping("/authentification/users/by-tenant/{tenantId}")
    void deleteUsersByTenant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );
    @PostMapping("/authentification/register-sip")
    void registerSipUser(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterUserRequest userRequest
    );
}