package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.LicenceType;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;
/*
    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody CreateTenantRequest request) {
        try {
            Tenant createdTenant = tenantService.createTenant(request);
            return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }*/

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody CreateTenantRequest request) {
        Tenant createdTenant = tenantService.createTenant(request);
        return ResponseEntity.ok(createdTenant);
    }

}