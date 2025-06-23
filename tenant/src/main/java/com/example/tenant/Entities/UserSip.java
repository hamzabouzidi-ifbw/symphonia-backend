package com.example.tenant.Entities;

import javax.persistence.*;

@Entity
@Table(name = "usersip")
public class UserSip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codeSip;

    private String username;
    private String contextNameSip;
    private String emailSip;             // Email de contact principal

    private String phoneSip;             // Numéro de téléphone

    private String domainNameSip;           // domain

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    // getters/setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeSip() {
        return codeSip;
    }

    public void setCodeSip(String codeSip) {
        this.codeSip = codeSip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContextNameSip() {
        return contextNameSip;
    }

    public void setContextNameSip(String contextNameSip) {
        this.contextNameSip = contextNameSip;
    }

    public String getEmailSip() {
        return emailSip;
    }

    public void setEmailSip(String emailSip) {
        this.emailSip = emailSip;
    }

    public String getPhoneSip() {
        return phoneSip;
    }

    public void setPhoneSip(String phoneSip) {
        this.phoneSip = phoneSip;
    }

    public String getDomainNameSip() {
        return domainNameSip;
    }

    public void setDomainNameSip(String domainNameSip) {
        this.domainNameSip = domainNameSip;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
