package com.example.tenant.Repositories;

import com.example.tenant.Dto.TrunkWithPoolDTO;
import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    List<Trunk> findByTenant(Tenant tenant);
    @Query("""
        SELECT new com.example.tenant.Dto.TrunkWithPoolDTO(
            t.name,
            t.username,
            t.proxy,
            t.realm,
            p.countryCode,
            p.areaCode,
            p.localCode,
            CAST(p.startNumber AS string),
            CAST(p.endNumber AS string)
        )
        FROM Trunk t
        JOIN TrunkPool p ON p.trunk.id = t.id
    """)
    List<TrunkWithPoolDTO> findAllTrunksWithPools();
}

