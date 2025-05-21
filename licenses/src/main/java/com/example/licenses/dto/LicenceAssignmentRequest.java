package com.example.licenses.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LicenceAssignmentRequest {
    private UUID licenceDefinitionId;
    private Long tenantId;
    private int maxUsers;
    private int usedUsers;


    public int getUsedUsers() {
        return usedUsers;
    }

    public void setUsedUsers(int usedUsers) {
        this.usedUsers = usedUsers;
    }




    public UUID getLicenceDefinitionId() {
        return licenceDefinitionId;
    }

    public void setLicenceDefinitionId(UUID licenceDefinitionId) {
        this.licenceDefinitionId = licenceDefinitionId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }
}