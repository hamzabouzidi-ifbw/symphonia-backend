package com.example.tenant.Dto;


public class CreateTenantRequest {
    private String Name;
    private String adminEmail;

    // Getters et setters
    public String getName() {
        return Name;
    }

    public void setName(String tenantName) {
        this.Name = tenantName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}
