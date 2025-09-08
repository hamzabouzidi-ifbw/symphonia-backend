package com.example.tenant.Repositories;

import com.example.tenant.Entities.CallLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallLimitRepository extends JpaRepository<CallLimit, Long> {
    Optional<CallLimit> findBySipProfileId(Long sipProfileId);

}
