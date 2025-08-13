package com.example.tenant.Services;

import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Repositories.TrunkPoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrunkPoolService {

    private final TrunkPoolRepository poolRepo;

    public TrunkPoolService(TrunkPoolRepository poolRepo) {
        this.poolRepo = poolRepo;
    }

    public TrunkPool createPool(TrunkPool pool) {
        return poolRepo.save(pool);
    }

    public List<TrunkPool> getActivePoolsByTenant(Long tenantId) {
        return poolRepo.findByTenantIdAndActiveTrue(tenantId);
    }

    public void deletePool(Long poolId) {
        poolRepo.deleteById(poolId);
    }

    public TrunkPool createPoolForTenant(Long tenantId, Long trunkId,
                                         String countryCode, String areaCode, String localCode,
                                         int startNumber, int endNumber) {
        TrunkPool pool = new TrunkPool();

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        pool.setTenant(tenant);

        Trunk trunk = new Trunk();
        trunk.setId(trunkId);
        pool.setTrunk(trunk);

        pool.setCountryCode(countryCode);
        pool.setAreaCode(areaCode);
        pool.setLocalCode(localCode);
        pool.setStartNumber(startNumber);
        pool.setEndNumber(endNumber);
        pool.setActive(true);

        return poolRepo.save(pool);
    }

    public boolean isNumberInTenantPool(Long tenantId, String number) {
        List<TrunkPool> pools = poolRepo.findByTenantIdAndActiveTrue(tenantId);

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

}
