package com.example.tenant.Entities.UsersConfig;

import com.example.tenant.Entities.SipProfile;

import javax.persistence.*;

@Entity
@Table(name = "voicemail_configs")
public class VoicemailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_profile_id", referencedColumnName = "id")
    private SipProfile sipProfile;

    private String voicemailPassword;

    private boolean voicemailEnabled;

    private String notificationEmail;

    // getters & setters


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

    public String getVoicemailPassword() {
        return voicemailPassword;
    }

    public void setVoicemailPassword(String voicemailPassword) {
        this.voicemailPassword = voicemailPassword;
    }

    public boolean isVoicemailEnabled() {
        return voicemailEnabled;
    }

    public void setVoicemailEnabled(boolean voicemailEnabled) {
        this.voicemailEnabled = voicemailEnabled;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
}

