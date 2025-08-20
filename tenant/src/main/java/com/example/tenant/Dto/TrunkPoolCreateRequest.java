package com.example.tenant.Dto;

import java.util.List;

public class TrunkPoolCreateRequest {
    private String countryCode;
    private String areaCode;
    private String localCode;
    private int startNumber;
    private int endNumber;

    private List<TrunkRequest> trunks;

    // getters / setters


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

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public int getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(int endNumber) {
        this.endNumber = endNumber;
    }

    public List<TrunkRequest> getTrunks() {
        return trunks;
    }

    public void setTrunks(List<TrunkRequest> trunks) {
        this.trunks = trunks;
    }

    public static class TrunkRequest {
        public String name;
        public String username;
        public String password;
        public String realm;
        public String proxy;
        public boolean registerEnabled;
        public String registerProxy;
        public String outboundProxy;
        public Integer proxyPort;
        public Integer expireSeconds;
        public Integer retrySeconds;
        public String registerTransport;
        public String fromUser;
        public String fromDomain;
        public Boolean callerIdInFrom;
        public String extension;
        public String contactParams;
        public String inboundCodecPrefs;
        public String outboundCodecPrefs;
        public Boolean secureSip;
        public Boolean secureRtp;
        public String sslCaCert;
        public String sslCert;
        public String sslKey;
        public Integer rtpTimeoutSec;
        public Integer minimumSessionExpires;
        public Integer sessionTimeout;
        public Boolean sessionTimers;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
}

