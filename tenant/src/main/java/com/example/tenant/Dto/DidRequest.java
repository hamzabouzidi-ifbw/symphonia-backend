package com.example.tenant.Dto;


import com.example.tenant.Entities.UsersConfig.DestinationType;
public class DidRequest {
    private Long tenantId;          // ID du tenant
    private Long trunkId;           // ID du trunk
    private String number;          // Numéro DID complet
    private DestinationType destinationType;
    private String destinationValue;

    // getters et setters


    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getTrunkId() {
        return trunkId;
    }

    public void setTrunkId(Long trunkId) {
        this.trunkId = trunkId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestinationValue() {
        return destinationValue;
    }

    public void setDestinationValue(String destinationValue) {
        this.destinationValue = destinationValue;
    }
}
