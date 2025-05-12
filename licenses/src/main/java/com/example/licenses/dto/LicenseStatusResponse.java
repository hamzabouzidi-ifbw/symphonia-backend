package com.example.licenses.dto;

import com.example.licenses.Entities.LicenseStatus;

public class LicenseStatusResponse {

    private Long tenantId;
    private LicenseStatus status;

    // ✅ Ajout du constructeur nécessaire
    public LicenseStatusResponse(Long tenantId, LicenseStatus status) {
        this.tenantId = tenantId;
        this.status = status;
    }

    // Getters et setters (ou @Data si tu utilises Lombok)
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    public void setStatus(LicenseStatus status) {
        this.status = status;
    }
}