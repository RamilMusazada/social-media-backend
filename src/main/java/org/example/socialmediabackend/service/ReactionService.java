package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionService {
    private final PostReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    private User getUserFromUserDetails(UserDetails userDetails) {
        log.info("Getting user from UserDetails");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new IllegalArgumentException("User not authenticated");
        }

        String email = userDetails.getUsername();
        log.info("Looking up user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Successfully retrieved user with email: {}", email);
        return user;
    }

    @Transactional
    public ReactionResponseDto reactToPost(ReactionRequestDto reactionRequestDto, UserDetails userDetails) {
        log.info("Processing reaction to post ID: {} with reaction type: {}",
                reactionRequestDto.getPostId(), reactionRequestDto.getReactionType());

        User currentUser = getUserFromUserDetails(userDetails);
        Post post = postRepository.findById(reactionRequestDto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + reactionRequestDto.getPostId()));

        Optional<PostReaction> existingReaction = reactionRepository.findByPostAndUser(post, currentUser);

        PostReaction reaction;
        if (existingReaction.isPresent()) {
            if (existingReaction.get().getReactionType() == reactionRequestDto.getReactionType()) {
                reactionRepository.delete(existingReaction.get());
                log.info("Removed {} reaction on post: {} by user: {}",
                        reactionRequestDto.getReactionType(), post.getId(), currentUser.getEmail());

                ReactionResponseDto response = getReactionCounts(post.getId(), currentUser.getId());
                log.info("Successfully processed reaction removal for post ID: {}", post.getId());
                return response;
            } else {
                reaction = existingReaction.get();
                reaction.setReactionType(reactionRequestDto.getReactionType());
                log.info("Updated reaction to {} on post: {} by user: {}",
                        reactionRequestDto.getReactionType(), post.getId(), currentUser.getEmail());
            }
        } else {
            reaction = PostReaction.builder()
                    .post(post)
                    .user(currentUser)
                    .reactionType(reactionRequestDto.getReactionType())
                    .build();
            log.info("Added new {} reaction on post: {} by user: {}",
                    reactionRequestDto.getReactionType(), post.getId(), currentUser.getEmail());
        }

        reactionRepository.save(reaction);
        ReactionResponseDto response = getReactionCounts(post.getId(), currentUser.getId());

        log.info("Successfully processed reaction for post ID: {} by user: {}",
                post.getId(), currentUser.getEmail());
        return response;
    }

    @Transactional
    public ReactionResponseDto removeReaction(Long postId, UserDetails userDetails) {
        log.info("Removing reaction from post ID: {}", postId);

        User currentUser = getUserFromUserDetails(userDetails);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        reactionRepository.deleteByPostAndUser(post, currentUser);

        ReactionResponseDto response = getReactionCounts(postId, currentUser.getId());
        log.info("Successfully removed reaction on post: {} by user: {}", postId, currentUser.getEmail());
        return response;
    }

    public ReactionResponseDto getReactionCounts(Long postId, Long userId) {
        log.info("Getting reaction counts for post ID: {} and user ID: {}", postId, userId);

        long likesCount = reactionRepository.countLikesByPostId(postId);
        long dislikesCount = reactionRepository.countDislikesByPostId(postId);

        Optional<PostReaction.ReactionType> userReaction = reactionRepository
                .findReactionTypeByPostIdAndUserId(postId, userId);

        ReactionResponseDto response = ReactionResponseDto.builder()
                .postId(postId)
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .userReaction(userReaction.orElse(null))
                .build();

        log.info("Successfully retrieved reaction counts for post ID: {} (likes: {}, dislikes: {}, user reaction: {})",
                postId, likesCount, dislikesCount, userReaction.orElse(null));
        return response;
    }

    public ReactionResponseDto getPostReactionDetails(Long postId, UserDetails userDetails) {
        log.info("Getting post reaction details for post ID: {}", postId);

        User currentUser = getUserFromUserDetails(userDetails);

        if (!postRepository.existsById(postId)) {
            log.error("Post not found with ID: {}", postId);
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        ReactionResponseDto response = getReactionCounts(postId, currentUser.getId());
        log.info("Successfully retrieved post reaction details for post ID: {}", postId);
        return response;
    }
}