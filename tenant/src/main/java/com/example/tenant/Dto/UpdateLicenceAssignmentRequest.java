package com.example.tenant.Dto;

import java.util.UUID;

public class UpdateLicenceAssignmentRequest {
    private UUID licenceDefinitionId;
    private int usedUsers;

    // Constructeurs
    public UpdateLicenceAssignmentRequest() {
    }

    public UpdateLicenceAssignmentRequest(UUID licenceDefinitionId, int usedUsers) {
        this.licenceDefinitionId = licenceDefinitionId;
        this.usedUsers = usedUsers;
    }

    // Getters et Setters
    public UUID getLicenceDefinitionId() {
        return licenceDefinitionId;
    }

    public void setLicenceDefinitionId(UUID licenceDefinitionId) {
        this.licenceDefinitionId = licenceDefinitionId;
    }

    public int getUsedUsers() {
        return usedUsers;
    }

    public void setUsedUsers(int usedUsers) {
        this.usedUsers = usedUsers;
    }

    // Méthode toString pour le logging/debug
    @Override
    public String toString() {
        return "UpdateLicenceAssignmentRequest{" +
                "licenceDefinitionId=" + licenceDefinitionId +
                ", usedUsers=" + usedUsers +
                '}';
    }
}