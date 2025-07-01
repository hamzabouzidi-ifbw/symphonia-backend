package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SipProfileRepository extends JpaRepository<SipProfile, Long> {

   Optional<SipProfile> findByExtension(String extension);

    List<SipProfile> findByTenantId(Long tenantId);

    List<SipProfile> findAllByTenantId(Long tenantId);

    Optional<SipProfile> findByUsername(String userName);
    Optional<SipProfile> findByEmail(String email);
    boolean existsByEmail(String email);

}