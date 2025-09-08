package com.example.tenant.Dto;

public class RestrictionRequest {
    private String domain;
    private String extension;
    private int maxCalls;
    private String dialplanRegex;
    private boolean allowedDialplan;
    private String startTime;
    private String endTime;
    private boolean allowTime;

    // Getters et setters
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
    public int getMaxCalls() { return maxCalls; }
    public void setMaxCalls(int maxCalls) { this.maxCalls = maxCalls; }
    public String getDialplanRegex() { return dialplanRegex; }
    public void setDialplanRegex(String dialplanRegex) { this.dialplanRegex = dialplanRegex; }
    public boolean isAllowedDialplan() { return allowedDialplan; }
    public void setAllowedDialplan(boolean allowedDialplan) { this.allowedDialplan = allowedDialplan; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public boolean isAllowTime() { return allowTime; }
    public void setAllowTime(boolean allowTime) { this.allowTime = allowTime; }
}
