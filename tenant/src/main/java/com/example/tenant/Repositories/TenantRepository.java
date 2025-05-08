package com.example.tenant.Repositories;

import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByAdminEmail(String adminEmail);
    boolean existsByCode(String code);
    // Suppose que tu as un champ `adminEmail` dans Tenant

}