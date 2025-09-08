package com.example.tenant.Repositories;

import com.example.tenant.Entities.DialPlanRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialPlanRestrictionRepository extends JpaRepository<DialPlanRestriction, Long> {
    List<DialPlanRestriction> findBySipProfileId(Long sipProfileId);
}
