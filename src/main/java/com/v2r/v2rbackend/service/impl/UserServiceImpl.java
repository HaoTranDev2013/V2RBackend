package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.dto.request.CreateUserRequest;
import com.v2r.v2rbackend.entity.Role;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.RoleRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import com.v2r.v2rbackend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public List<User> findAllWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Override
    public Page<User> findAllWithRoles(Pageable pageable) {
        return userRepository.findAllWithRoles(pageable);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        if (!userRepository.existsById(user.getUserID())) {
            throw new EntityNotFoundException("User not found with id: " + user.getUserID());
        }

        // Check if email is being changed and if new email already exists
        Optional<User> existingUser = userRepository.findById(user.getUserID());
        if (existingUser.isPresent() && !existingUser.get().getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByStatus(boolean status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public Page<User> findByStatus(boolean status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }

    @Override
    public List<User> findByIsVerified(boolean isVerified) {
        return userRepository.findByIsVerified(isVerified);
    }

    @Override
    public Page<User> findByIsVerified(boolean isVerified, Pageable pageable) {
        return userRepository.findByIsVerified(isVerified, pageable);
    }

    @Override
    public User updateStatus(Integer id, boolean status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Override
    public User verifyUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setVerified(true);
        return userRepository.save(user);
    }

    @Override
    public User updateLoyaltyPoints(Integer id, int points) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setLoyaltyPoints(points);
        return userRepository.save(user);
    }

    @Override
    public User createUser(CreateUserRequest request) {
        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + request.getRoleId()));

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        user.setRole(role);
        user.setStatus(true);
        user.setVerified(true);
        user.setLoyaltyPoints(0);

        return userRepository.save(user);
    }
}
