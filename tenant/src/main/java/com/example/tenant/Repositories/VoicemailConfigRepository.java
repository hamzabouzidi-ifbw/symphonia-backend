package com.example.tenant.Repositories;

import com.example.tenant.Entities.UsersConfig.VoicemailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoicemailConfigRepository extends JpaRepository<VoicemailConfig, Long> {
    Optional<VoicemailConfig> findBySipProfileId(Long sipProfileId);
}

