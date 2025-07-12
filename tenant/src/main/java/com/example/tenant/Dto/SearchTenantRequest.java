package com.example.tenant.Dto;

import java.time.LocalDateTime;

public class SearchTenantRequest {
    private String tenantName;
    private String contextName;
    private String code;
    private Integer extensionPrefix;
    private LocalDateTime createdAt;
    private String timezone;

    // Getters and Setters


    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getExtensionPrefix() {
        return extensionPrefix;
    }

    public void setExtensionPrefix(Integer extensionPrefix) {
        this.extensionPrefix = extensionPrefix;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}

