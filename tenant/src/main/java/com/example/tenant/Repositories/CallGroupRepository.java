package com.example.tenant.Repositories;

import com.example.tenant.Entities.UsersConfig.CallGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallGroupRepository extends JpaRepository<CallGroup, Long> {
    List<CallGroup> findByMembers_Id(Long sipProfileId);
}
