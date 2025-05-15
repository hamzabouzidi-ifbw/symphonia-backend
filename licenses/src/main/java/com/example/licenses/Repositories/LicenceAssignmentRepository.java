package com.example.licenses.Repositories;

import com.example.licenses.Entities.LicenceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LicenceAssignmentRepository extends JpaRepository<LicenceAssignment, UUID> {

    List<LicenceAssignment> findByTenantId(Long tenantId);

    List<LicenceAssignment> findByActiveTrue();

    List<LicenceAssignment> findByEndDateBeforeAndActiveTrue(java.time.LocalDate date);
}