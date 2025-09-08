package com.example.tenant.Repositories;

import com.example.tenant.Entities.TimeRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeRestrictionRepository extends JpaRepository<TimeRestriction, Long> {
    List<TimeRestriction> findBySipProfileId(Long sipProfileId);
}

