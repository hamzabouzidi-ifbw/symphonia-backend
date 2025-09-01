package com.example.tenant.Dto;

public class TrunkCreateDTO {
    private String name;
    private boolean active = true;
    private String username;
    private String password;
    private String realm;
    private String proxy;
    private boolean registerEnabled = true;
    private String registerProxy;
    private String outboundProxy;
    private Integer proxyPort;
    private Integer expireSeconds = 3600;
    private Integer retrySeconds = 30;
    private String registerTransport = "UDP";
    private String fromUser;
    private String fromDomain;
    private Boolean callerIdInFrom = true;
    private String extension;
    private String contactParams;
    private String inboundCodecPrefs;
    private String outboundCodecPrefs;
    private Boolean secureSip = false;
    private Boolean secureRtp = false;
    private String sslCaCert;
    private String sslCert;
    private String sslKey;
    private Integer rtpTimeoutSec = 300;
    private Integer minimumSessionExpires = 90;
    private Integer sessionTimeout = 1800;
    private Boolean sessionTimers = true;

    // getters / setters (tu peux générer automatiquement dans IDE)


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public boolean isRegisterEnabled() {
        return registerEnabled;
    }

    public void setRegisterEnabled(boolean registerEnabled) {
        this.registerEnabled = registerEnabled;
    }

    public String getRegisterProxy() {
        return registerProxy;
    }

    public void setRegisterProxy(String registerProxy) {
        this.registerProxy = registerProxy;
    }

    public String getOutboundProxy() {
        return outboundProxy;
    }

    public void setOutboundProxy(String outboundProxy) {
        this.outboundProxy = outboundProxy;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public Integer getRetrySeconds() {
        return retrySeconds;
    }

    public void setRetrySeconds(Integer retrySeconds) {
        this.retrySeconds = retrySeconds;
    }

    public String getRegisterTransport() {
        return registerTransport;
    }

    public void setRegisterTransport(String registerTransport) {
        this.registerTransport = registerTransport;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromDomain() {
        return fromDomain;
    }

    public void setFromDomain(String fromDomain) {
        this.fromDomain = fromDomain;
    }

    public Boolean getCallerIdInFrom() {
        return callerIdInFrom;
    }

    public void setCallerIdInFrom(Boolean callerIdInFrom) {
        this.callerIdInFrom = callerIdInFrom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContactParams() {
        return contactParams;
    }

    public void setContactParams(String contactParams) {
        this.contactParams = contactParams;
    }

    public String getInboundCodecPrefs() {
        return inboundCodecPrefs;
    }

    public void setInboundCodecPrefs(String inboundCodecPrefs) {
        this.inboundCodecPrefs = inboundCodecPrefs;
    }

    public String getOutboundCodecPrefs() {
        return outboundCodecPrefs;
    }

    public void setOutboundCodecPrefs(String outboundCodecPrefs) {
        this.outboundCodecPrefs = outboundCodecPrefs;
    }

    public Boolean getSecureSip() {
        return secureSip;
    }

    public void setSecureSip(Boolean secureSip) {
        this.secureSip = secureSip;
    }

    public Boolean getSecureRtp() {
        return secureRtp;
    }

    public void setSecureRtp(Boolean secureRtp) {
        this.secureRtp = secureRtp;
    }

    public String getSslCaCert() {
        return sslCaCert;
    }

    public void setSslCaCert(String sslCaCert) {
        this.sslCaCert = sslCaCert;
    }

    public String getSslCert() {
        return sslCert;
    }

    public void setSslCert(String sslCert) {
        this.sslCert = sslCert;
    }

    public String getSslKey() {
        return sslKey;
    }

    public void setSslKey(String sslKey) {
        this.sslKey = sslKey;
    }

    public Integer getRtpTimeoutSec() {
        return rtpTimeoutSec;
    }

    public void setRtpTimeoutSec(Integer rtpTimeoutSec) {
        this.rtpTimeoutSec = rtpTimeoutSec;
    }

    public Integer getMinimumSessionExpires() {
        return minimumSessionExpires;
    }

    public void setMinimumSessionExpires(Integer minimumSessionExpires) {
        this.minimumSessionExpires = minimumSessionExpires;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Boolean getSessionTimers() {
        return sessionTimers;
    }

    public void setSessionTimers(Boolean sessionTimers) {
        this.sessionTimers = sessionTimers;
    }
}
