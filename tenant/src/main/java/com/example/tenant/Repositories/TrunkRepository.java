package com.example.tenant.Repositories;

import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    List<Trunk> findByTenant(Tenant tenant);

    List<Trunk> findByTenantIdAndActiveTrue(Long tenantId);
    List<Trunk> findAll();
    List<Trunk> findByTenantId(Long tenantId);
}
