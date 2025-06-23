package com.example.tenant.Dto;

import lombok.Data;

import java.util.UUID;
@Data

public class LicenceAssignationToUserRequest  {
    private UUID licenceDefinitionId;
    private Long userSipId;


    public UUID getLicenceDefinitionId() {
        return licenceDefinitionId;
    }

    public void setLicenceDefinitionId(UUID licenceDefinitionId) {
        this.licenceDefinitionId = licenceDefinitionId;
    }

    public Long getUserSipId() {
        return userSipId;
    }

    public void setUserSipId(Long userSipId) {
        this.userSipId = userSipId;
    }
}
