package com.example.tenant.Entities;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;


    private String tenantName;       // Nom de l'entreprise ou organisation
    private String contextName;
    private String email;             // Email de contact principal

    private String phone;             // Numéro de téléphone

    private String address;           // Adresse


    private String domainName;           // domain

    private boolean active = true;    // Pour activer/désactiver un tenant
    private String adminEmail;
    private String timezone="Tunisia";
    @Column(nullable = false)
    private Integer extensionPrefix; // Préfixe numérique (ex: 1 pour tenant A)

    @Column(nullable = false)
    private Integer nextExtensionNumber = 1; // Prochain numéro à attribuer (commence à 1)

    @Column(nullable = false)
    private Integer extensionLength = 3;
    private LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


    public Integer getExtensionPrefix() {
        return extensionPrefix;
    }

    public void setExtensionPrefix(Integer extensionPrefix) {
        this.extensionPrefix = extensionPrefix;
    }

    public Integer getNextExtensionNumber() {
        return nextExtensionNumber;
    }

    public void setNextExtensionNumber(Integer nextExtensionNumber) {
        this.nextExtensionNumber = nextExtensionNumber;
    }

    public Integer getExtensionLength() {
        return extensionLength;
    }

    public void setExtensionLength(Integer extensionLength) {
        this.extensionLength = extensionLength;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }



    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
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