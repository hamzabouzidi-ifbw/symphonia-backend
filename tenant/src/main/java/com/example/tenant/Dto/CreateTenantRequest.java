package com.example.tenant.Dto;


import java.util.List;

public class CreateTenantRequest {
    private String name;
    private String email;
    private String companyName;
    private String address;
    private String phone;
    private String domain;
    private String adminEmail;
    private List<LicenceAssignmentRequest> licences;

    public List<LicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<LicenceAssignmentRequest> licences) {
        this.licences = licences;
    }

    // Getters et setters
    public String getName() {
        return name;
    }

    public void setName(String tenantName) {
        this.name = tenantName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}