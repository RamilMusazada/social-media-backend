package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
import org.example.socialmediabackend.dto.FollowStatsDto;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.dto.UserProfileWithFollowDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.exception.UnauthorizedAccessException;
import org.example.socialmediabackend.model.Follow;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.FollowRepository;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class FollowService {
    private static final Logger logger = Logger.getLogger(FollowService.class.getName());

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("User not authenticated");
        }

        String email = userDetails.getUsername();
        logger.info("Looking up user with email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public void followUser(String targetUserEmail, UserDetails currentUserDetails) {
        User currentUser = getUserFromUserDetails(currentUserDetails);
        User targetUser = getUserByEmail(targetUserEmail);

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            logger.info("User " + currentUser.getEmail() + " is already following " + targetUser.getEmail());
            return;
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);
        logger.info("User " + currentUser.getEmail() + " is now following " + targetUser.getEmail());
    }

    @Transactional
    public void unfollowUser(String targetUserEmail, UserDetails currentUserDetails) {
        User currentUser = getUserFromUserDetails(currentUserDetails);
        User targetUser = getUserByEmail(targetUserEmail);

        Optional<Follow> followRelationship = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        if (followRelationship.isPresent()) {
            followRepository.delete(followRelationship.get());
            logger.info("User " + currentUser.getEmail() + " has unfollowed " + targetUser.getEmail());
        } else {
            logger.info("No follow relationship found to delete");
        }
    }

    public PaginatedResponseDto<UserProfileDto> getFollowers(String userEmail, int page, int size, UserDetails currentUserDetails) {
        User targetUser = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> followers = followRepository.findFollowers(targetUser, pageable);

        List<UserProfileDto> followerDtos = followers.getContent().stream()
                .map(UserProfileDto::fromUser)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                followerDtos,
                followers.getNumber(),
                followers.getSize(),
                followers.getTotalElements(),
                followers.getTotalPages(),
                followers.isLast()
        );
    }

    public PaginatedResponseDto<UserProfileDto> getFollowing(String userEmail, int page, int size, UserDetails currentUserDetails) {
        User targetUser = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> following = followRepository.findFollowing(targetUser, pageable);

        List<UserProfileDto> followingDtos = following.getContent().stream()
                .map(UserProfileDto::fromUser)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                followingDtos,
                following.getNumber(),
                following.getSize(),
                following.getTotalElements(),
                following.getTotalPages(),
                following.isLast()
        );
    }

    public FollowStatsDto getFollowStats(String userEmail, UserDetails currentUserDetails) {
        User targetUser = getUserByEmail(userEmail);
        User currentUser = null;
        boolean isFollowing = false;

        if (currentUserDetails != null) {
            try {
                currentUser = getUserFromUserDetails(currentUserDetails);
                isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
            } catch (Exception e) {
                logger.warning("Failed to get current user or follow status: " + e.getMessage());
            }
        }

        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);

        return FollowStatsDto.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    public UserProfileWithFollowDto getUserProfileWithFollowStats(String userEmail, UserDetails currentUserDetails) {
        User targetUser = getUserByEmail(userEmail);
        FollowStatsDto followStats = getFollowStats(userEmail, currentUserDetails);

        return UserProfileWithFollowDto.fromUserAndStats(targetUser, followStats);
    }
}