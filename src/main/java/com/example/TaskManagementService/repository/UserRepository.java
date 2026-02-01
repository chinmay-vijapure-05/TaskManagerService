package com.example.TaskManagementService.repository;

import com.example.TaskManagementService.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "users", key = "#email")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}