package com.example.tenant.Dto;

public class RestrictionUpdateRequest {
    private String domain;
    private String extension;
    private Integer maxCalls;
    private Boolean ignoreTransfer;
    private String dialplanRegex;
    private Boolean allowedDialplan;
    private String startTime;
    private String endTime;
    private Boolean allowTime;

    // getters / setters


    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Integer getMaxCalls() {
        return maxCalls;
    }

    public void setMaxCalls(Integer maxCalls) {
        this.maxCalls = maxCalls;
    }

    public Boolean getIgnoreTransfer() {
        return ignoreTransfer;
    }

    public void setIgnoreTransfer(Boolean ignoreTransfer) {
        this.ignoreTransfer = ignoreTransfer;
    }

    public String getDialplanRegex() {
        return dialplanRegex;
    }

    public void setDialplanRegex(String dialplanRegex) {
        this.dialplanRegex = dialplanRegex;
    }

    public Boolean getAllowedDialplan() {
        return allowedDialplan;
    }

    public void setAllowedDialplan(Boolean allowedDialplan) {
        this.allowedDialplan = allowedDialplan;
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

    public Boolean getAllowTime() {
        return allowTime;
    }

    public void setAllowTime(Boolean allowTime) {
        this.allowTime = allowTime;
    }
}
