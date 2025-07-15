package com.example.tenant.Dto;

public class VoicemailConfigDto {
    private Long id;
    private Long sipProfileId;
    private String voicemailPassword;
    private boolean voicemailEnabled;
    private String notificationEmail;
    private String sipUserExtension;

    // Getters & Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSipProfileId() {
        return sipProfileId;
    }

    public void setSipProfileId(Long sipProfileId) {
        this.sipProfileId = sipProfileId;
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

    public String getSipUserExtension() {
        return sipUserExtension;
    }

    public void setSipUserExtension(String sipUserExtension) {
        this.sipUserExtension = sipUserExtension;
    }
}