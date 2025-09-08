package com.example.tenant.Entities;

import javax.persistence.*;

@Entity
@Table(name = "dialplan_restrictions")
public class DialPlanRestriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_profile_id", nullable = false)
    private SipProfile sipProfile;

    @Column(nullable = false)
    private String regex; // ex: "^00\d+"

    @Column(nullable = false)
    private boolean allowed; // true = autorisé, false = rejeté

    // Getters/Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SipProfile getSipProfile() {
        return sipProfile;
    }

    public void setSipProfile(SipProfile sipProfile) {
        this.sipProfile = sipProfile;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}

