
package com.example.tenant.Entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;


@Entity
@Table(name = "trunks")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Trunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------- Informations générales -----------
    private String name;       // Nom du trunk (interne)
    private boolean active = true;

    // ----------- Authentification -----------
    private String username;   // Nom d’utilisateur SIP
    private String password;   // Mot de passe SIP
    private String realm;      // Domaine d’authentification

    // ----------- Connectivité -----------
    private String proxy;          // Proxy SIP
    private boolean registerEnabled = true; // true si enregistrement nécessaire
    private String registerProxy;  // Proxy spécifique pour enregistrement
    private String outboundProxy;  // Proxy spécifique pour appels sortants
    private Integer proxyPort;     // Port SIP (ex: 5060, 5962…)

    // ----------- Paramètres d’enregistrement -----------
    private Integer expireSeconds = 3600;   // Timer enregistrement
    private Integer retrySeconds = 30;      // Intervalle retry
    private String registerTransport = "UDP"; // UDP, TCP, TLS

    // ----------- Présentation d’appel -----------
    private String fromUser;
    private String fromDomain;
    private Boolean callerIdInFrom = true;
    private String extension; // Extension interne associée

    // ----------- Transport et codecs -----------
    private String contactParams;
    private String inboundCodecPrefs;   // ex: "PCMU,PCMA,G729"
    private String outboundCodecPrefs;  // ex: "PCMU,PCMA,G729"

    // ----------- Sécurité et chiffrement -----------
    private Boolean secureSip = false;  // TLS
    private Boolean secureRtp = false;  // SRTP
    private String sslCaCert;
    private String sslCert;
    private String sslKey;

    // ----------- Timers -----------
    private Integer rtpTimeoutSec = 300;
    private Integer minimumSessionExpires = 90;
    private Integer sessionTimeout = 1800;
    private Boolean sessionTimers = true;

    // ----------- Multi-tenant -----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    // Ajout relation inverse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trunk_pool_id") // nom de la colonne dans la base
    @JsonManagedReference
    private TrunkPool trunkPool;

    // Getter et Setter
    public TrunkPool getTrunkPool() {
        return trunkPool;
    }

    public void setTrunkPool(TrunkPool trunkPool) {
        this.trunkPool = trunkPool;
    }

    //getters and setters



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
