package com.example.tenant.Controllers;




import com.example.tenant.Dto.CreateTenantRequest;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<String> createTenant(@RequestBody CreateTenantRequest request) {
        tenantService.createTenant(request);
        return ResponseEntity.ok("Tenant et admin créés avec succès.");
    }
}
