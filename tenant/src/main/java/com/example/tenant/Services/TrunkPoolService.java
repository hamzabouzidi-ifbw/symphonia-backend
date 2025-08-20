package com.example.tenant.Services;

import com.example.tenant.Dto.TrunkPoolCreateRequest;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkPoolRepository;
import com.example.tenant.Repositories.TrunkRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrunkPoolService {

    private final TrunkPoolRepository trunkPoolRepository;
    private final TrunkRepository trunkRepository;
    private final TenantRepository tenantRepository;

    public TrunkPoolService(TrunkPoolRepository trunkPoolRepository,
                            TrunkRepository trunkRepository,
                            TenantRepository tenantRepository) {
        this.trunkPoolRepository = trunkPoolRepository;
        this.trunkRepository = trunkRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<TrunkPool> getActivePoolsByTenant(Long tenantId) {
        return trunkPoolRepository.findByTenantIdAndActiveTrue(tenantId);
    }



    public boolean isNumberInTenantPool(Long tenantId, String number) {
        List<TrunkPool> pools = trunkPoolRepository.findByTenantIdAndActiveTrue(tenantId);

        for (TrunkPool pool : pools) {
            String prefix = pool.getCountryCode() + pool.getAreaCode() + pool.getLocalCode();
            if (number.startsWith(prefix)) {
                try {
                    int localPart = Integer.parseInt(number.substring(prefix.length()));
                    if (localPart >= pool.getStartNumber() && localPart <= pool.getEndNumber()) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // Ce n'est pas un numéro valide
                }
            }
        }
        return false;
    }



    @Transactional
    public TrunkPool createTrunkPool(Long tenantId, TrunkPoolCreateRequest request) {
        // Récupération du Tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Création du TrunkPool
        TrunkPool trunkPool = new TrunkPool();
        trunkPool.setTenant(tenant);
        trunkPool.setCountryCode(request.getCountryCode());
        trunkPool.setAreaCode(request.getAreaCode());
        trunkPool.setLocalCode(request.getLocalCode());
        trunkPool.setStartNumber(request.getStartNumber());
        trunkPool.setEndNumber(request.getEndNumber());
        trunkPool.setActive(true);

        // Initialisation de la liste des Trunks
        trunkPool.setTrunks(new ArrayList<>());

        // --- Cas 1 : plusieurs trunks ---
        if (request.getTrunks() != null && !request.getTrunks().isEmpty()) {
            for (TrunkPoolCreateRequest.TrunkRequest tReq : request.getTrunks()) {
                Trunk trunk = buildTrunkFromRequest(tReq, tenant, trunkPool);
                trunkPool.getTrunks().add(trunk);
            }
        }
        // --- Cas 2 : un seul trunk ---
        else if (request.getSingleTrunk() != null) {
            Trunk trunk = buildTrunkFromRequest(request.getSingleTrunk(), tenant, trunkPool);
            trunkPool.getTrunks().add(trunk);
        }

        // Définir le Trunk principal si au moins un trunk existe
        if (!trunkPool.getTrunks().isEmpty()) {
            trunkPool.setTrunk(trunkPool.getTrunks().get(0));
        }

        // Sauvegarde unique grâce au cascade
        trunkPoolRepository.save(trunkPool);

        return trunkPool;
    }

    /**
     * Méthode utilitaire pour construire un Trunk à partir d'un TrunkRequest
     */
    private Trunk buildTrunkFromRequest(TrunkPoolCreateRequest.TrunkRequest tReq, Tenant tenant, TrunkPool trunkPool) {
        Trunk trunk = new Trunk();
        trunk.setTenant(tenant);
        trunk.setName(tReq.getName());
        trunk.setUsername(tReq.getUsername());
        trunk.setPassword(tReq.getPassword());
        trunk.setRealm(tReq.getRealm());
        trunk.setProxy(tReq.getProxy());
        trunk.setRegisterEnabled(tReq.isRegisterEnabled());
        trunk.setRegisterProxy(tReq.getRegisterProxy());
        trunk.setOutboundProxy(tReq.getOutboundProxy());
        trunk.setProxyPort(tReq.getProxyPort());
        trunk.setExpireSeconds(tReq.getExpireSeconds());
        trunk.setRetrySeconds(tReq.getRetrySeconds());
        trunk.setRegisterTransport(tReq.getRegisterTransport());
        trunk.setFromUser(tReq.getFromUser());
        trunk.setFromDomain(tReq.getFromDomain());
        trunk.setCallerIdInFrom(tReq.getCallerIdInFrom());
        trunk.setExtension(tReq.getExtension());
        trunk.setContactParams(tReq.getContactParams());
        trunk.setInboundCodecPrefs(tReq.getInboundCodecPrefs());
        trunk.setOutboundCodecPrefs(tReq.getOutboundCodecPrefs());
        trunk.setSecureSip(tReq.getSecureSip());
        trunk.setSecureRtp(tReq.getSecureRtp());
        trunk.setSslCaCert(tReq.getSslCaCert());
        trunk.setSslCert(tReq.getSslCert());
        trunk.setSslKey(tReq.getSslKey());
        trunk.setRtpTimeoutSec(tReq.getRtpTimeoutSec());
        trunk.setMinimumSessionExpires(tReq.getMinimumSessionExpires());
        trunk.setSessionTimeout(tReq.getSessionTimeout());
        trunk.setSessionTimers(tReq.getSessionTimers());
        trunk.setActive(true);

        // Lien bidirectionnel
        trunk.setTrunkPool(trunkPool);

        return trunk;
    }



}