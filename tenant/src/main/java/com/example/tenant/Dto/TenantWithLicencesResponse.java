package com.example.tenant.Dto;

import com.example.tenant.Entities.Tenant;

import java.util.List;

public class TenantWithLicencesResponse {
    private Tenant tenant;
    private List<LicenceAssignmentRequest> licences;

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public List<LicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<LicenceAssignmentRequest> licences) {
        this.licences = licences;
    }
}
