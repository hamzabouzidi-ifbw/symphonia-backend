package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SipProfileRepository extends JpaRepository<SipProfile, Long> {

 Optional<SipProfile> findByExtension(String extension);
 boolean existsByExtension(String extension);

 List<SipProfile> findAllByTenantId(Long tenantId);

 Optional<SipProfile> findByUsername(String userName);
 Optional<SipProfile> findByEmail(String email);
 boolean existsByEmail(String email);

 Optional<SipProfile> findByDomainNameAndExtensionAndActiveTrue(String domainName, String extension);

 @Query("SELECT s FROM SipProfile s WHERE s.tenantId = :tenantId AND s.active = true")
 List<SipProfile> findActiveByTenant(@Param("tenantId") Long tenantId);

}