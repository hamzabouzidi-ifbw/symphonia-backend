package com.example.tenant.Dto;

import lombok.Data;

import java.util.List;
@Data

public class UserSipRequest {
    private Long tenantId;      // ID du tenant lié
    private String username;
    private String email;
    private String phone;
    private String domain;
    private String context;

    private List<LicenceAssignmentRequest> licences;


    public List<LicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<LicenceAssignmentRequest> licences) {
        this.licences = licences;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
