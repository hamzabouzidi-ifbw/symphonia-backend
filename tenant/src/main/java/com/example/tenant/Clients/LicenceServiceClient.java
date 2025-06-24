package com.example.tenant.Clients;

import com.example.tenant.Dto.LicenceAssignmentRequest;
import com.example.tenant.Dto.MultipleLicenceAssignmentRequest;
import com.example.tenant.Dto.UpdateLicenceAssignmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @PutMapping("/update-assignment-user/{tenantId}")
    ResponseEntity<?> updateLicenceAssignment(@PathVariable Long tenantId,
                                              @RequestBody UpdateLicenceAssignmentRequest request,
                                              @RequestHeader("Authorization") String token);

    @DeleteMapping("/by-tenant/{sipUserId}")
    void deleteLicencesBySipUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long sipUserId
    );
}