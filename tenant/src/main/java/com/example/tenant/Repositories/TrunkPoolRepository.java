package com.example.tenant.Repositories;

import com.example.tenant.Entities.TrunkPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrunkPoolRepository extends JpaRepository<TrunkPool, Long> {
    List<TrunkPool> findByTenantIdAndActiveTrue(Long tenantId);
}

