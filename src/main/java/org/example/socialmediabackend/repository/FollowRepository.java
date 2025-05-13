package org.example.socialmediabackend.repository;

import org.example.socialmediabackend.model.Follow;
import org.example.socialmediabackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
    Page<User> findFollowing(@Param("user") User user, Pageable pageable);

    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
    Page<User> findFollowers(@Param("user") User user, Pageable pageable);

    long countByFollowing(User following);

    long countByFollower(User follower);

    void deleteByFollowerAndFollowing(User follower, User following);
}