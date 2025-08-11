package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DidNumberRepository extends JpaRepository<DidNumber, Long> {

    List<DidNumber> findByTenantAndActiveTrue(Tenant tenant);
    Optional<DidNumber> findByNumberAndActiveTrue(String number);
    List<DidNumber> findByTenantIdAndActiveTrue(Long tenantId);
    boolean existsByNumber(String number);



}
