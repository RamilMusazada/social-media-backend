package org.example.socialmediabackend.repository;

import org.example.socialmediabackend.model.Post;
import org.example.socialmediabackend.model.PostReaction;
import org.example.socialmediabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostAndUser(Post post, User user);

    boolean existsByPostAndUser(Post post, User user);

    long countByPostAndReactionType(Post post, PostReaction.ReactionType reactionType);

    void deleteByPostAndUser(Post post, User user);

    @Query("SELECT COUNT(pr) FROM PostReaction pr WHERE pr.post.id = :postId AND pr.reactionType = 'LIKE'")
    long countLikesByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(pr) FROM PostReaction pr WHERE pr.post.id = :postId AND pr.reactionType = 'DISLIKE'")
    long countDislikesByPostId(@Param("postId") Long postId);

    @Query("SELECT pr.reactionType FROM PostReaction pr WHERE pr.post.id = :postId AND pr.user.id = :userId")
    Optional<PostReaction.ReactionType> findReactionTypeByPostIdAndUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId);
}