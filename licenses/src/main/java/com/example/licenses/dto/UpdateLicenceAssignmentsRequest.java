package com.example.licenses.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateLicenceAssignmentsRequest {
    private Long tenantId;
    private List<UpdateLicenceAssignmentRequest> licences;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<UpdateLicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<UpdateLicenceAssignmentRequest> licences) {
        this.licences = licences;
    }
// Getters et Setters
}