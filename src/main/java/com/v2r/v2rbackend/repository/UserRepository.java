package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(boolean status);
    Page<User> findByStatus(boolean status, Pageable pageable);
    List<User> findByIsVerified(boolean isVerified);
    Page<User> findByIsVerified(boolean isVerified, Pageable pageable);
    Optional<User> findByEmailAndStatus(String email, boolean status);
    
    // Using EntityGraph to eagerly fetch role - more efficient than JOIN FETCH for pagination
    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT u FROM User u")
    List<User> findAllWithRoles();
    
    @EntityGraph(attributePaths = {"role"})
    @Query(value = "SELECT u FROM User u",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);
}
