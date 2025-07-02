package com.example.tenant.Clients;

import com.example.tenant.Dto.RegisterUserRequest;
import com.example.tenant.Entities.SipProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "authentification", url = "${auth.service.url}")
public interface AuthServiceClient {

    @PostMapping("/authentification/register_tenant")
    void registerTenantUser(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterUserRequest userRequest
    );

    @PostMapping("/authentification/register-sip")
    void registerSipUser(@RequestHeader("Authorization") String token,
                         @RequestBody RegisterUserRequest userRequest);
    @DeleteMapping("/authentification/users/by-tenant/{tenantId}")
    void deleteUsersByTenant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );

    @DeleteMapping("/authentification/users/by-tenant/{tenantId}")
    void deleteSipUsers(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );
    @GetMapping("/authentification/users/check-email")
    Boolean checkIfEmailExists(@RequestHeader("Authorization") String token,
                               @RequestParam("email") String email);

    @DeleteMapping("/authentification/users/by-email")
    void deleteUserByEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam("email") String email
    );
    @GetMapping("/authentification/users/exists")
    Boolean checkIfUserExists(@RequestParam("email") String email, @RequestHeader("Authorization") String authToken);

    @PutMapping("/authentification/users/deactivate-by-tenant/{tenantId}")
    void deactivateUsersByTenant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );
}