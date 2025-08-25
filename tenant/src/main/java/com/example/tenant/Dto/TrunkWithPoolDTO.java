package com.example.tenant.Dto;

public class TrunkWithPoolDTO {
    private Long poolId;

    private String trunkName;
    private String username;
    private String proxy;
    private String realm;
    private String countryCode;
    private String areaCode;
    private String localCode;
    private String startNumber;
    private String endNumber;

    public TrunkWithPoolDTO(String trunkName, String username, String proxy, String realm,
                            String countryCode, String areaCode, String localCode,
                            String startNumber, String endNumber) {
        this.trunkName = trunkName;
        this.username = username;
        this.proxy = proxy;
        this.realm = realm;
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.localCode = localCode;
        this.startNumber = startNumber;
        this.endNumber = endNumber;
    }

    // Getters & Setters


    public String getTrunkName() {
        return trunkName;
    }

    public void setTrunkName(String trunkName) {
        this.trunkName = trunkName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public String getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(String startNumber) {
        this.startNumber = startNumber;
    }

    public String getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(String endNumber) {
        this.endNumber = endNumber;
    }
}
