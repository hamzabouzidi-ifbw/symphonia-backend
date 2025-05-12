package com.example.licenses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignLicenseRequest {
    private Long tenantId;
    private List<String> licenseKeys; // Liste des clés de licence
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxUsers;
    // ====== Getters ======


    public List<String> getLicenseKeys() {
        return licenseKeys;
    }

    public void setLicenseKeys(List<String> licenseKeys) {
        this.licenseKeys = licenseKeys;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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