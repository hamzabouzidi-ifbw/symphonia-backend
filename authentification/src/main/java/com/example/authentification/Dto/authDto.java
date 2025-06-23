package com.example.authentification.Dto;

public class authDto {
    private String email;
    private String password;
    private String role;
    private Long tenantId;
    private Long UserSipId;

    // Getters et Setters

    public Long getUserSipId() {
        return UserSipId;
    }

    public void setUserSipId(Long userSipId) {
        UserSipId = userSipId;
    }

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