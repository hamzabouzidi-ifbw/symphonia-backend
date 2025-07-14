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
    private String extension;
    @Column(name = "sip_profile_id")
    private Long sipProfileId;

    private String domainName;

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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getSipProfileId() {
        return sipProfileId;
    }

    public void setSipProfileId(Long sipProfileId) {
        this.sipProfileId = sipProfileId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
