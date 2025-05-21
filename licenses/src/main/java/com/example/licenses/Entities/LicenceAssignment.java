package com.example.licenses.Entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class LicenceAssignment {
    @Id @GeneratedValue
    private UUID id;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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