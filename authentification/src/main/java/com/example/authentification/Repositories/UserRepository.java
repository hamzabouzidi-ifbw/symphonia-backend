package com.example.authentification.Repositories;

import com.example.authentification.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User findByTenantId(Long tenantId);
    List<User> findUsersByTenantId(Long tenantId);


}