package com.example.tenant.Repositories;

import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByTenantName(String tenantName);
    Optional<Tenant> findByDomainName(String domainName);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByAdminEmail(String adminEmail);
    Optional<Tenant> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Tenant> findByTenantNameAndIdNot(String tenantName, Long id);
    Optional<Tenant> findByDomainNameAndIdNot(String domainName, Long id);
    Optional<Tenant> findByEmailAndIdNot(String email, Long id);
    Optional<Tenant> findByContextName(String contextName);

    boolean existsByExtensionPrefix(Integer extensionPrefix);
}