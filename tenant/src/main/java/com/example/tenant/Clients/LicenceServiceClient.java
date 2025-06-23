package com.example.tenant.Clients;

import com.example.tenant.Dto.LicenceAssignationToUserRequest;
import com.example.tenant.Dto.LicenceAssignmentRequest;
import com.example.tenant.Dto.MultipleLicenceAssignmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.net.http.HttpHeaders;
import java.util.List;

@FeignClient(name = "licence-service", url = "${licence.service.url}")
public interface LicenceServiceClient {

    @PostMapping("/assign-multiple")
    void assignMultipleLicences(
            @RequestHeader("Authorization") String token,
            @RequestBody MultipleLicenceAssignmentRequest request
    );

    @GetMapping("/by-tenant/{tenantId}")
    List<LicenceAssignmentRequest> getLicencesByTenant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );

    @DeleteMapping("/by-tenant/{tenantId}")
    void deleteLicencesByTenant(
            @RequestHeader("Authorization") String token,
            @PathVariable Long tenantId
    );

    @GetMapping("/licences/tenant/{tenantId}")
    List<LicenceAssignmentRequest> getLicencesByTenantId(
            @PathVariable("tenantId") Long tenantId,
            @RequestHeader("Authorization") String token
    );


    // Nouvelle méthode pour assigner une licence à un user SIP
    @PostMapping("/licences/assign-to-user")
    void assignLicenceToUserSip(@RequestHeader("Authorization") String token,
                                @RequestBody LicenceAssignationToUserRequest assignRequest);
}