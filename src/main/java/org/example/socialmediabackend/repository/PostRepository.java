package org.example.socialmediabackend.repository;

import org.example.socialmediabackend.model.Post;
import org.example.socialmediabackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    Optional<Post> findByIdAndAuthor(Long id, User author);

    boolean existsByIdAndAuthor(Long id, User author);


    @Query("SELECT p FROM Post p WHERE p.author.id IN " +
            "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsFromFollowedUsers(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT p FROM Post p WHERE p.author.id != :userId ORDER BY p.createdAt DESC")
    Page<Post> findPostsExcludingUser(@Param("userId") Long userId, Pageable pageable);
}