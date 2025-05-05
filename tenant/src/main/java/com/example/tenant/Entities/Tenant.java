package com.example.tenant.Entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[a-z0-9_]+$")
    private String code;

    @Column(nullable = false)
    @Size(min = 2, max = 100)
    private String name;

    @Email
    private String email;

    private String phone;
    private String address;

    @Column(nullable = false)
    private String contextName;

    private String domainName;
    @Column(nullable = false, unique = true)
    @Email
    private String admin_tenant_email;

    @Column(nullable = false, unique = true)
    private String admin_tenant_password;



    // Remplace le champ licenseType String
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenantLicense> licenses = new ArrayList<>();

    @Column(nullable = false)
    private Integer maxUsers;

    private String codecPreference;
    private Boolean mohEnabled;
    private Boolean ivrEnabled;
    private String timeZone;


    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    @Lob
    private String dialplanXml;

    // Audit
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

}
