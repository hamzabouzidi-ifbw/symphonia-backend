package com.example.tenant.Repositories;

import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> , JpaSpecificationExecutor<Tenant> {

    Optional<Tenant> findByTenantName(String tenantName);
    Optional<Tenant> findByDomainName(String domainName);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByAdminEmail(String adminEmail);
    Optional<Tenant> findByTenantNameAndIdNot(String tenantName, Long id);
    Optional<Tenant> findByDomainNameAndIdNot(String domainName, Long id);
    Optional<Tenant> findByEmailAndIdNot(String email, Long id);
    boolean existsByExtensionPrefix(Integer extensionPrefix);
}