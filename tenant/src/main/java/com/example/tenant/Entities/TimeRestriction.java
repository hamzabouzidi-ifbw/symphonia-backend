package com.example.tenant.Entities;

import javax.persistence.*;

@Entity
@Table(name = "time_restrictions")
public class TimeRestriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_profile_id", nullable = false)
    private SipProfile sipProfile;

    @Column(nullable = false)
    private String startTime; // HH:mm

    @Column(nullable = false)
    private String endTime;   // HH:mm

    @Column(nullable = false)
    private boolean allow;    // true = autorisé, false = interdit

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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean allow) {
        this.allow = allow;
    }
}

