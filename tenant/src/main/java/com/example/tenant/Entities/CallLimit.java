package com.example.tenant.Entities;

import javax.persistence.*;

@Entity
@Table(name = "call_limits")
public class CallLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_profile_id", nullable = false)
    private SipProfile sipProfile;

    @Column(nullable = false)
    private Integer maxConcurrentCalls;

    private boolean ignoreTransfer = false; // facultatif


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

    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public boolean isIgnoreTransfer() {
        return ignoreTransfer;
    }

    public void setIgnoreTransfer(boolean ignoreTransfer) {
        this.ignoreTransfer = ignoreTransfer;
    }
}

