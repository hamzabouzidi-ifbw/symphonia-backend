package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SipProfileRepository extends JpaRepository<SipProfile, UUID> {

    // Trouver un profil SIP par username
    SipProfile findByUsername(String username);

    // Trouver un profil SIP par email
    SipProfile findByEmail(String email);

    // Trouver un profil SIP par extension
    SipProfile findByExtension(String extension);

    // Trouver tous les profils SIP d'un tenant
    List<SipProfile> findByTenantId(Long tenantId);

    // Trouver tous les profils SIP actifs/inactifs d'un tenant
    List<SipProfile> findByTenantIdAndActive(Long tenantId, boolean active);

    // Vérifier si un username existe déjà
    boolean existsByUsername(String username);

    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);

    // Vérifier si une extension existe déjà
    boolean existsByExtension(String extension);

    // Trouver les profils SIP par licence
    List<SipProfile> findByLicenceDefinitionId(UUID licenceDefinitionId);

    // Trouver les profils SIP par tenant et licence
    List<SipProfile> findByTenantIdAndLicenceDefinitionId(Long tenantId, UUID licenceDefinitionId);
}