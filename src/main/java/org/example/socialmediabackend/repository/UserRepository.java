package org.example.socialmediabackend.repository;

import org.example.socialmediabackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT * FROM users u WHERE " +
            "(:firstName IS NULL OR LOWER(CAST(u.first_name AS text)) LIKE LOWER(CONCAT('%', CAST(:firstName AS text), '%'))) AND " +
            "(:lastName IS NULL OR LOWER(CAST(u.last_name AS text)) LIKE LOWER(CONCAT('%', CAST(:lastName AS text), '%'))) AND " +
            "(:username IS NULL OR LOWER(CAST(u.username AS text)) LIKE LOWER(CONCAT('%', CAST(:username AS text), '%'))) AND " +
            "u.enabled = true",
            nativeQuery = true)
    Page<User> searchUsers(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("username") String username,
            Pageable pageable);
}