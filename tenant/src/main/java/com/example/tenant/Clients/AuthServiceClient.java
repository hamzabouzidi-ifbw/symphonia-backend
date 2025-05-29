package com.example.tenant.Clients;

import com.example.tenant.Dto.RegisterUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
}