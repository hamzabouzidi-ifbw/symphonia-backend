package com.example.tenant.Dto;

public class TrunkPoolCreateDTO {
    private int countryCode;
    private int areaCode;
    private int localCode;
    private int startNumber;
    private int endNumber;

    // getters / setters
    public int getCountryCode() { return countryCode; }
    public void setCountryCode(int countryCode) { this.countryCode = countryCode; }

    public int getAreaCode() { return areaCode; }
    public void setAreaCode(int areaCode) { this.areaCode = areaCode; }

    public int getLocalCode() { return localCode; }
    public void setLocalCode(int localCode) { this.localCode = localCode; }

    public int getStartNumber() { return startNumber; }
    public void setStartNumber(int startNumber) { this.startNumber = startNumber; }

    public int getEndNumber() { return endNumber; }
    public void setEndNumber(int endNumber) { this.endNumber = endNumber; }
}
