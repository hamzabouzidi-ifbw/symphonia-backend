package com.example.tenant.Dto;

import javax.persistence.Column;
import java.util.List;

public class UpdateTenantRequest {



    private String TenantName;       // Nom de l'entreprise ou organisation
    private String contextName;
    private String email;             // Email de contact principal

    private String phone;             // Numéro de téléphone
    private String code;
    private String address;           // Adresse


    private String domainName;           // domain









    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTenantName() {
        return TenantName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTenantName(String tenantName) {
        TenantName = tenantName;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}