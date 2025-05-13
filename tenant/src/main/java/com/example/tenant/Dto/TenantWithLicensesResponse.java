package com.example.tenant.Dto;

import java.util.List;

public class TenantWithLicensesResponse {
    private Long id;
    private String name;
    private String domain;
    private String email;
    private String adminEmail;
    private List<String> licenseKeys;
    private String companyName;       // Nom de l'entreprise ou organisation
    private String code;
    private String phone;             // Numéro de téléphone

    private String address;           // Adresse    private boolean active = true;    // Pour activer/désactiver un tenant


    // Getters et setters


    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public List<String> getLicenseKeys() {
        return licenseKeys;
    }

    public void setLicenseKeys(List<String> licenseKeys) {
        this.licenseKeys = licenseKeys;
    }
}
