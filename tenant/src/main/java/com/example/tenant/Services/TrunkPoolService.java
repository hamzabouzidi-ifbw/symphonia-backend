package com.example.tenant.Services;

import com.example.tenant.Dto.TrunkCreateDTO;
import com.example.tenant.Dto.TrunkPoolCreateDTO;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkPoolRepository;
import com.example.tenant.Repositories.TrunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrunkPoolService {
    @Autowired
    private TrunkPoolRepository trunkPoolRepository;

    @Autowired
    private TrunkRepository trunkRepository;
    @Autowired
    private TenantRepository tenantRepository;

    // Créer un pool pour un tenant
    public TrunkPool createTrunkPool(Long tenantId,
                                     int countryCode,
                                     int areaCode,
                                     int localCode,
                                     int startNumber,
                                     int endNumber) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        TrunkPool trunkPool = new TrunkPool();
        trunkPool.setTenant(tenant);
        trunkPool.setCountryCode(countryCode);
        trunkPool.setAreaCode(areaCode);
        trunkPool.setLocalCode(localCode);
        trunkPool.setStartNumber(startNumber);
        trunkPool.setEndNumber(endNumber);
        trunkPool.setActive(true);

        return trunkPoolRepository.save(trunkPool);
    }

    // Ajouter un ou plusieurs trunks à un pool
    public Trunk createTrunkForPool(Long poolId, TrunkCreateDTO dto) {
        TrunkPool trunkPool = trunkPoolRepository.findById(poolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool not found"));

        Trunk trunk = new Trunk();
        trunk.setTrunkPool(trunkPool);
        trunk.setTenant(trunkPool.getTenant()); // <-- Ici, on set le tenant automatiquement
        trunk.setName(dto.getName());
        trunk.setActive(dto.isActive());
        trunk.setUsername(dto.getUsername());
        trunk.setPassword(dto.getPassword());
        trunk.setRealm(dto.getRealm());
        trunk.setProxy(dto.getProxy());
        trunk.setRegisterEnabled(dto.isRegisterEnabled());
        trunk.setRegisterProxy(dto.getRegisterProxy());
        trunk.setOutboundProxy(dto.getOutboundProxy());
        trunk.setProxyPort(dto.getProxyPort());
        trunk.setExpireSeconds(dto.getExpireSeconds());
        trunk.setRetrySeconds(dto.getRetrySeconds());
        trunk.setRegisterTransport(dto.getRegisterTransport());
        trunk.setFromUser(dto.getFromUser());
        trunk.setFromDomain(dto.getFromDomain());
        trunk.setCallerIdInFrom(dto.getCallerIdInFrom());
        trunk.setExtension(dto.getExtension());
        trunk.setContactParams(dto.getContactParams());
        trunk.setInboundCodecPrefs(dto.getInboundCodecPrefs());
        trunk.setOutboundCodecPrefs(dto.getOutboundCodecPrefs());
        trunk.setSecureSip(dto.getSecureSip());
        trunk.setSecureRtp(dto.getSecureRtp());
        trunk.setSslCaCert(dto.getSslCaCert());
        trunk.setSslCert(dto.getSslCert());
        trunk.setSslKey(dto.getSslKey());
        trunk.setRtpTimeoutSec(dto.getRtpTimeoutSec());
        trunk.setMinimumSessionExpires(dto.getMinimumSessionExpires());
        trunk.setSessionTimeout(dto.getSessionTimeout());
        trunk.setSessionTimers(dto.getSessionTimers());

        Trunk savedTrunk = trunkRepository.save(trunk);

        trunkPool.getTrunks().add(savedTrunk);
        trunkPoolRepository.save(trunkPool);

        return savedTrunk;
    }

    public TrunkPool updateTrunkPool(Long poolId, TrunkPoolCreateDTO dto) {
        TrunkPool trunkPool = trunkPoolRepository.findById(poolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool not found"));

        trunkPool.setCountryCode(dto.getCountryCode());
        trunkPool.setAreaCode(dto.getAreaCode());
        trunkPool.setLocalCode(dto.getLocalCode());
        trunkPool.setStartNumber(dto.getStartNumber());
        trunkPool.setEndNumber(dto.getEndNumber());

        return trunkPoolRepository.save(trunkPool);
    }

    public Trunk updateTrunk(Long trunkId, TrunkCreateDTO dto) {
        Trunk trunk = trunkRepository.findById(trunkId)
                .orElseThrow(() -> new RuntimeException("Trunk not found"));

        trunk.setName(dto.getName());
        trunk.setActive(dto.isActive());
        trunk.setUsername(dto.getUsername());
        trunk.setPassword(dto.getPassword());
        trunk.setRealm(dto.getRealm());
        trunk.setProxy(dto.getProxy());
        trunk.setRegisterEnabled(dto.isRegisterEnabled());
        trunk.setRegisterProxy(dto.getRegisterProxy());
        trunk.setOutboundProxy(dto.getOutboundProxy());
        trunk.setProxyPort(dto.getProxyPort());
        trunk.setExpireSeconds(dto.getExpireSeconds());
        trunk.setRetrySeconds(dto.getRetrySeconds());
        trunk.setRegisterTransport(dto.getRegisterTransport());
        trunk.setFromUser(dto.getFromUser());
        trunk.setFromDomain(dto.getFromDomain());
        trunk.setCallerIdInFrom(dto.getCallerIdInFrom());
        trunk.setExtension(dto.getExtension());
        trunk.setContactParams(dto.getContactParams());
        trunk.setInboundCodecPrefs(dto.getInboundCodecPrefs());
        trunk.setOutboundCodecPrefs(dto.getOutboundCodecPrefs());
        trunk.setSecureSip(dto.getSecureSip());
        trunk.setSecureRtp(dto.getSecureRtp());
        trunk.setSslCaCert(dto.getSslCaCert());
        trunk.setSslCert(dto.getSslCert());
        trunk.setSslKey(dto.getSslKey());
        trunk.setRtpTimeoutSec(dto.getRtpTimeoutSec());
        trunk.setMinimumSessionExpires(dto.getMinimumSessionExpires());
        trunk.setSessionTimeout(dto.getSessionTimeout());
        trunk.setSessionTimers(dto.getSessionTimers());

        return trunkRepository.save(trunk);
    }


    public TrunkPool getTrunkPool(Long poolId) {
        return trunkPoolRepository.findById(poolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool not found"));
    }
    public List<TrunkPool> getAllTrunkPools() {
        return trunkPoolRepository.findAll();
    }

    public List<Trunk> getTrunksByTrunkPool(Long poolId) {
        TrunkPool trunkPool = trunkPoolRepository.findById(poolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool not found"));

        // Retourne juste la liste des trunks
        return new ArrayList<>(trunkPool.getTrunks());
    }
    public void deleteTrunkPool(Long poolId) {
        TrunkPool trunkPool = trunkPoolRepository.findById(poolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool not found"));

        trunkPoolRepository.delete(trunkPool);
    }
    public void deleteTrunk(Long trunkId) {
        Trunk trunk = trunkRepository.findById(trunkId)
                .orElseThrow(() -> new RuntimeException("Trunk not found"));

        trunkRepository.delete(trunk);
    }

    public List<Trunk> getActiveTrunksByTenant(Long tenantId) {
        return trunkRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    //configuration freeswitch
    public List<TrunkPool> getActivePoolsByTenant(Long tenantId) {
        return trunkPoolRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    public List<Trunk> getAllTrunks() {
        return trunkRepository.findAll();
    }

    public List<Trunk> getTrunksByTenant(Long tenantId) {
        return trunkRepository.findByTenantId(tenantId);
    }

}