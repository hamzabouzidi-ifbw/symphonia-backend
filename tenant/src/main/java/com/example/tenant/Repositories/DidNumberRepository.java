package com.example.tenant.Repositories;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DidNumberRepository extends JpaRepository<DidNumber, Long> {

        Optional<DidNumber> findByDidNumber(String didNumber);
        List<DidNumber> findBySipProfile(SipProfile sipProfile);
        List<DidNumber> findBySipProfileId(Long sipProfileId); // Optionnel

}

