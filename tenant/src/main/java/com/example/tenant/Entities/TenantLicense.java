package com.example.tenant.Entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenant_licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "license_definition_id", nullable = false)
    private UUID licenseDefinitionId; // Référence à la licence dans license-service

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer allocatedUsers;

    @Column(nullable = false)
    private Integer usedUsers = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status;


    public boolean isActive() {
        return status == LicenseStatus.ACTIVE &&
                endDate.isAfter(LocalDate.now());
    }
}