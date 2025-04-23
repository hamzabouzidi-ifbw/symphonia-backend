package com.example.licenses.Repositories;

import com.example.licenses.Entities.Role;
import com.example.licenses.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRole(Role role);
    boolean existsByEmail(String email);

}
