package com.example.licenses.Repositories;

import com.example.licenses.Entities.LicenseStatus;
import com.example.licenses.Entities.TenantLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<TenantLicense, UUID> {
    Optional<TenantLicense> findByTenantId(UUID tenantId);
    List<TenantLicense> findByStatus(LicenseStatus status);
}