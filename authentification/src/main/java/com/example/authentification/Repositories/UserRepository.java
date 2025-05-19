package com.example.authentification.Repositories;

import com.example.authentification.Entities.Role;
import com.example.authentification.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRole(Role role);
    boolean existsByEmail(String email);
    List<User> findByTenantId(Long tenantId);
}