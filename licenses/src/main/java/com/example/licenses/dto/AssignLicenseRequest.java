package com.example.licenses.dto;

import com.example.licenses.Entities.LicenseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignLicenseRequest {
    private UUID tenantId;
    private String licenseKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxUsers;
    // ====== Getters ======


    public UUID getTenantId() {
        return tenantId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getMaxUsers() {
        return maxUsers;
    }



    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }




}