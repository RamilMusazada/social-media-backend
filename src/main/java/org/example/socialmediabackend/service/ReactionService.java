package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
import org.example.socialmediabackend.dto.ReactionRequestDto;
import org.example.socialmediabackend.dto.ReactionResponseDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.model.Post;
import org.example.socialmediabackend.model.PostReaction;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.PostReactionRepository;
import org.example.socialmediabackend.repository.PostRepository;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ReactionService {
    private static final Logger logger = Logger.getLogger(ReactionService.class.getName());

    private final PostReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ReactionService(
            PostReactionRepository reactionRepository,
            PostRepository postRepository,
            UserRepository userRepository) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        String email = userDetails.getUsername();
        logger.info("Looking up user with email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public ReactionResponseDto reactToPost(ReactionRequestDto reactionRequestDto, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);
        Post post = postRepository.findById(reactionRequestDto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + reactionRequestDto.getPostId()));

        Optional<PostReaction> existingReaction = reactionRepository.findByPostAndUser(post, currentUser);

        PostReaction reaction;
        if (existingReaction.isPresent()) {
            if (existingReaction.get().getReactionType() == reactionRequestDto.getReactionType()) {
                reactionRepository.delete(existingReaction.get());
                logger.info("Removed " + reactionRequestDto.getReactionType() +
                        " reaction on post: " + post.getId() +
                        " by user: " + currentUser.getEmail());

                return getReactionCounts(post.getId(), currentUser.getId());
            }
            else {
                reaction = existingReaction.get();
                reaction.setReactionType(reactionRequestDto.getReactionType());
                logger.info("Updated reaction to " + reactionRequestDto.getReactionType() +
                        " on post: " + post.getId() +
                        " by user: " + currentUser.getEmail());
            }
        } else {
            reaction = PostReaction.builder()
                    .post(post)
                    .user(currentUser)
                    .reactionType(reactionRequestDto.getReactionType())
                    .build();
            logger.info("Added new " + reactionRequestDto.getReactionType() +
                    " reaction on post: " + post.getId() +
                    " by user: " + currentUser.getEmail());
        }

        reactionRepository.save(reaction);
        return getReactionCounts(post.getId(), currentUser.getId());
    }

    @Transactional
    public ReactionResponseDto removeReaction(Long postId, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        reactionRepository.deleteByPostAndUser(post, currentUser);
        logger.info("Removed reaction on post: " + postId + " by user: " + currentUser.getEmail());

        return getReactionCounts(postId, currentUser.getId());
    }

    public ReactionResponseDto getReactionCounts(Long postId, Long userId) {
        long likesCount = reactionRepository.countLikesByPostId(postId);
        long dislikesCount = reactionRepository.countDislikesByPostId(postId);

        Optional<PostReaction.ReactionType> userReaction = reactionRepository
                .findReactionTypeByPostIdAndUserId(postId, userId);

        return ReactionResponseDto.builder()
                .postId(postId)
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .userReaction(userReaction.orElse(null))
                .build();
    }

    public ReactionResponseDto getPostReactionDetails(Long postId, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        return getReactionCounts(postId, currentUser.getId());
    }
}