package com.payvault.userservice.repository;

import com.payvault.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuthUsername(String authUsername);
    boolean existsByAuthUsername(String authUsername);
}
