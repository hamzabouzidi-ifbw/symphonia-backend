package com.example.tenant.Repositories;

import com.example.tenant.Entities.UserSip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSipRepository extends JpaRepository<UserSip, Long> {


    List<UserSip> findByTenantId(Long tenantId);

    Optional<UserSip> findByUsernameAndTenantId(String username, Long tenantId);

}

