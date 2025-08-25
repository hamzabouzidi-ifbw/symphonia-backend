package com.example.tenant.Services;

import com.example.tenant.Dto.TrunkWithPoolDTO;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Repositories.TenantRepository;
import com.example.tenant.Repositories.TrunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrunkService {

    @Autowired
    private TrunkRepository trunkRepository;

    @Autowired
    private TenantRepository tenantRepository;

    public Trunk createTrunk(Trunk trunk, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        trunk.setTenant(tenant);
        Trunk savedTrunk = trunkRepository.save(trunk);

        return savedTrunk;
    }

    public List<Trunk> getTrunksByTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return trunkRepository.findByTenant(tenant);
    }


    public List<TrunkWithPoolDTO> getAllTrunksWithPools() {
        return trunkRepository.findAllTrunksWithPools();
    }



}