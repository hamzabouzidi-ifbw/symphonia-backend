package com.example.tenant.Repositories;

import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    List<Trunk> findByTenant(Tenant tenant);
}

