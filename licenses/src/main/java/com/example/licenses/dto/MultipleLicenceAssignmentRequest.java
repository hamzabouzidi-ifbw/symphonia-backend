package com.example.licenses.dto;

import lombok.Data;

import java.util.List;

@Data
public class MultipleLicenceAssignmentRequest {
    private Long tenantId;
    private List<LicenceAssignmentRequest> licences;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<LicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<LicenceAssignmentRequest> licences) {
        this.licences = licences;
    }
}
