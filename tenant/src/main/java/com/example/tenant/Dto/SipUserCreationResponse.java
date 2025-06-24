package com.example.tenant.Dto;

import java.util.UUID;

public class SipUserCreationResponse {
    private UUID id;
    private String username;
    private String email;
    private String extension;
    private String domainName;
    private boolean active;
    private String password;
    private String message;

    public SipUserCreationResponse(UUID id, String username, String email, String extension, String domainName, boolean active, String password, String message) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.extension = extension;
        this.domainName = domainName;
        this.active = active;
        this.password = password;
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}