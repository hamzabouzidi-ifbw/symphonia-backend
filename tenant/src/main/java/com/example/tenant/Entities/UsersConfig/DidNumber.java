package com.example.tenant.Entities.UsersConfig;

import com.example.tenant.Entities.SipProfile;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "did_number")
public class DidNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("number")
    private String didNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_profile_id", referencedColumnName = "id")
    private SipProfile sipProfile;

    // Getters / Setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDidNumber() {
        return didNumber;
    }

    public void setDidNumber(String didNumber) {
        this.didNumber = didNumber;
    }

    public SipProfile getSipProfile() {
        return sipProfile;
    }

    public void setSipProfile(SipProfile sipProfile) {
        this.sipProfile = sipProfile;
    }

    // Méthode pratique pour accéder aux propriétés liées
    public String getExtension() {
        return sipProfile != null ? sipProfile.getExtension() : null;
    }

    public String getDomainName() {
        return sipProfile != null ? sipProfile.getDomainName() : null;
    }
}