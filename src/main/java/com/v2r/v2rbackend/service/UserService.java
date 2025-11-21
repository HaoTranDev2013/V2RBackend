package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.dto.request.CreateUserRequest;
import com.v2r.v2rbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    List<User> findAllWithRoles();
    Page<User> findAllWithRoles(Pageable pageable);
    Optional<User> findById(Integer id);
    User save(User user);
    User update(User user);
    void deleteById(Integer id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(boolean status);
    Page<User> findByStatus(boolean status, Pageable pageable);
    List<User> findByIsVerified(boolean isVerified);
    Page<User> findByIsVerified(boolean isVerified, Pageable pageable);
    User updateStatus(Integer id, boolean status);
    User verifyUser(Integer id);
    User updateLoyaltyPoints(Integer id, int points);
    User createUser(CreateUserRequest request);
}
