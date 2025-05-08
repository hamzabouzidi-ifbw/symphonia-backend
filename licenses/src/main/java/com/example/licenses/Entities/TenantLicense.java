package com.example.licenses.Entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenant_licenses")
@NoArgsConstructor
@AllArgsConstructor
public class TenantLicense {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID tenantId;
    private String licenseKey;

    private LocalDate startDate;
    private LocalDate endDate;

    private int maxUsers;
    private int usedUsers;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    // ====== Getters ======

    public UUID getId() {
        return id;
    }

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

    public int getUsedUsers() {
        return usedUsers;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    // ====== Setters ======

    public void setId(UUID id) {
        this.id = id;
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

    public void setUsedUsers(int usedUsers) {
        this.usedUsers = usedUsers;
    }

    public void setStatus(LicenseStatus status) {
        this.status = status;
    }
}