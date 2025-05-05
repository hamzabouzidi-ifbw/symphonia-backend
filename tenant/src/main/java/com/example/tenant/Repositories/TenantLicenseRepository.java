package com.example.tenant.Repositories;

import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.TenantLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantLicenseRepository  extends JpaRepository<TenantLicense, Long> {
}
