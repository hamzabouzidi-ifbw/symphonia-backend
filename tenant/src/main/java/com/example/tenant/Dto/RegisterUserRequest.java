package com.example.tenant.Dto;


public class RegisterUserRequest {
    private String email;
    private String password;
    private String role;
    private Long tenantId;

    public RegisterUserRequest() {
    }

    public RegisterUserRequest(String email, String password, String role, Long tenantId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
    }

    // Getters et setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}