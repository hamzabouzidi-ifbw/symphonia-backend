package com.example.tenant.Dto;

import java.util.UUID;

public class CreateSipUserRequest {
    private String username;
    private String email;
    private String password;
    private String extension;
    private Long tenantId;
    private UUID licenceDefinitionId;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getters and Setters
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



    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getLicenceDefinitionId() {
        return licenceDefinitionId;
    }

    public void setLicenceDefinitionId(UUID licenceDefinitionId) {
        this.licenceDefinitionId = licenceDefinitionId;
    }
}