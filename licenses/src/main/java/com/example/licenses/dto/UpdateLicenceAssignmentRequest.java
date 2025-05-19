package com.example.licenses.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateLicenceAssignmentRequest {
    private UUID licenceDefinitionId;
    private Integer maxUsers;

    private Boolean renew; // Si true, prolonge la date de fin à partir de la date actuelle

    public UUID getLicenceDefinitionId() {
        return licenceDefinitionId;
    }

    public void setLicenceDefinitionId(UUID licenceDefinitionId) {
        this.licenceDefinitionId = licenceDefinitionId;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Boolean getRenew() {
        return renew;
    }

    public void setRenew(Boolean renew) {
        this.renew = renew;
    }
// Getters et Setters
}