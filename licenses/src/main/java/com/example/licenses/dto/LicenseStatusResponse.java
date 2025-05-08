package com.example.licenses.dto;

import com.example.licenses.Entities.LicenseStatus;

import java.util.UUID;

public class LicenseStatusResponse {

    private UUID tenantId;
    private LicenseStatus status;

    // ✅ Ajout du constructeur nécessaire
    public LicenseStatusResponse(UUID tenantId, LicenseStatus status) {
        this.tenantId = tenantId;
        this.status = status;
    }

    // Getters et setters (ou @Data si tu utilises Lombok)
    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    public void setStatus(LicenseStatus status) {
        this.status = status;
    }
}