package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SipProfileRepository extends JpaRepository<SipProfile, Long> {

 Optional<SipProfile> findByExtension(String extension);
 boolean existsByExtension(String extension);


 List<SipProfile> findByTenantId(Long tenantId);

 List<SipProfile> findAllByTenantId(Long tenantId);

 Optional<SipProfile> findByUsername(String userName);
 Optional<SipProfile> findByEmail(String email);
 boolean existsByEmail(String email);
 List<SipProfile> findByDomainNameAndActiveTrue(String domainName);

 Optional<SipProfile> findByDomainNameAndUsernameAndActiveTrue(String domainName, String username);
 Optional<SipProfile> findByExtensionAndId(String extension, Long id);

}