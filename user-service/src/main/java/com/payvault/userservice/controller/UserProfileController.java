package com.payvault.userservice.controller;

import com.payvault.userservice.dto.UserProfileRequest;
import com.payvault.userservice.entity.UserProfile;
import com.payvault.userservice.repository.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileRepository repository;

    public UserProfileController(UserProfileRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<UserProfile> create(@Valid @RequestBody UserProfileRequest request) {
        if (repository.existsByAuthUsername(request.getAuthUsername())) {
            throw new IllegalArgumentException("Profile already exists for this user");
        }

        UserProfile profile = new UserProfile(
                request.getAuthUsername(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
        );
        UserProfile saved = repository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getById(@PathVariable Long id) {
        UserProfile profile = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User profile not found with id: " + id));
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/username/{authUsername}")
    public ResponseEntity<UserProfile> getByUsername(@PathVariable String authUsername) {
        UserProfile profile = repository.findByAuthUsername(authUsername)
                .orElseThrow(() -> new NoSuchElementException("No profile found for username: " + authUsername));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> update(@PathVariable Long id, @Valid @RequestBody UserProfileRequest request) {
        UserProfile profile = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User profile not found with id: " + id));

        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());

        return ResponseEntity.ok(repository.save(profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("User profile not found with id: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
