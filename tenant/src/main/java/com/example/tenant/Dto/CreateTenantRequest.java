package com.example.tenant.Dto;


public class CreateTenantRequest {
    private String name;
    private String adminEmail;

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
}
