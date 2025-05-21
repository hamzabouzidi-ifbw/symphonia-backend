package com.example.tenant.Dto;


import javax.persistence.Column;
import java.util.List;

public class CreateTenantRequest {



    private String code;


    private String tenantName;       // Nom de l'entreprise ou organisation
    private String contextName;
    private String email;             // Email de contact principal

    private String phone;             // Numéro de téléphone

    private String address;           // Adresse


    private String domainName;           // domain


    private String adminEmail;
    private List<LicenceAssignmentRequest> licences;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public List<LicenceAssignmentRequest> getLicences() {
        return licences;
    }

    public void setLicences(List<LicenceAssignmentRequest> licences) {
        this.licences = licences;
    }



    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }



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



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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


}
