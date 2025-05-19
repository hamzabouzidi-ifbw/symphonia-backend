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
    Optional<Tenant> findByCode(String code);
    // Suppose que tu as un champ `adminEmail` dans Tenant
    Optional<Tenant> findByNameAndIdNot(String name, Long id);
    Optional<Tenant> findByDomainAndIdNot(String domain, Long id);
    Optional<Tenant> findByEmailAndIdNot(String email, Long id);
}