package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    private User getUserFromUserDetails(UserDetails userDetails) {
        log.info("Getting user from UserDetails");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new UnauthorizedAccessException("User not authenticated");
        }

        String email = userDetails.getUsername();
        log.info("Looking up user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Successfully retrieved user with email: {}", email);
        return user;
    }

    private User getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Successfully retrieved user with email: {}", email);
        return user;
    }

    @Transactional
    public void followUser(String targetUserEmail, UserDetails currentUserDetails) {
        log.info("Starting follow operation for target user: {}", targetUserEmail);

        User currentUser = getUserFromUserDetails(currentUserDetails);
        User targetUser = getUserByEmail(targetUserEmail);

        if (currentUser.getId().equals(targetUser.getId())) {
            log.error("User {} attempted to follow themselves", currentUser.getEmail());
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            log.info("User {} is already following {}", currentUser.getEmail(), targetUser.getEmail());
            return;
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);
        log.info("Successfully created follow relationship: {} is now following {}",
                currentUser.getEmail(), targetUser.getEmail());
    }

    @Transactional
    public void unfollowUser(String targetUserEmail, UserDetails currentUserDetails) {
        log.info("Starting unfollow operation for target user: {}", targetUserEmail);

        User currentUser = getUserFromUserDetails(currentUserDetails);
        User targetUser = getUserByEmail(targetUserEmail);

        Optional<Follow> followRelationship = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        if (followRelationship.isPresent()) {
            followRepository.delete(followRelationship.get());
            log.info("Successfully removed follow relationship: {} has unfollowed {}",
                    currentUser.getEmail(), targetUser.getEmail());
        } else {
            log.info("No follow relationship found to delete between {} and {}",
                    currentUser.getEmail(), targetUser.getEmail());
        }
    }

    public PaginatedResponseDto<UserProfileDto> getFollowers(String userEmail, int page, int size, UserDetails currentUserDetails) {
        log.info("Getting followers for user: {} (page: {}, size: {})", userEmail, page, size);

        User targetUser = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> followers = followRepository.findFollowers(targetUser, pageable);

        List<UserProfileDto> followerDtos = followers.getContent().stream()
                .map(UserProfileDto::fromUser)
                .collect(Collectors.toList());

        PaginatedResponseDto<UserProfileDto> response = new PaginatedResponseDto<>(
                followerDtos,
                followers.getNumber(),
                followers.getSize(),
                followers.getTotalElements(),
                followers.getTotalPages(),
                followers.isLast()
        );

        log.info("Successfully retrieved {} followers for user: {} (total: {})",
                followerDtos.size(), userEmail, followers.getTotalElements());
        return response;
    }

    public PaginatedResponseDto<UserProfileDto> getFollowing(String userEmail, int page, int size, UserDetails currentUserDetails) {
        log.info("Getting following list for user: {} (page: {}, size: {})", userEmail, page, size);

        User targetUser = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> following = followRepository.findFollowing(targetUser, pageable);

        List<UserProfileDto> followingDtos = following.getContent().stream()
                .map(UserProfileDto::fromUser)
                .collect(Collectors.toList());

        PaginatedResponseDto<UserProfileDto> response = new PaginatedResponseDto<>(
                followingDtos,
                following.getNumber(),
                following.getSize(),
                following.getTotalElements(),
                following.getTotalPages(),
                following.isLast()
        );

        log.info("Successfully retrieved {} following users for user: {} (total: {})",
                followingDtos.size(), userEmail, following.getTotalElements());
        return response;
    }

    public FollowStatsDto getFollowStats(String userEmail, UserDetails currentUserDetails) {
        log.info("Getting follow stats for user: {}", userEmail);

        User targetUser = getUserByEmail(userEmail);
        User currentUser = null;
        boolean isFollowing = false;

        if (currentUserDetails != null) {
            currentUser = getUserFromUserDetails(currentUserDetails);
            isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        }

        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);

        FollowStatsDto stats = FollowStatsDto.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();

        log.info("Successfully retrieved follow stats for user: {} (followers: {}, following: {}, isFollowing: {})",
                userEmail, followerCount, followingCount, isFollowing);
        return stats;
    }

    public UserProfileWithFollowDto getUserProfileWithFollowStats(String userEmail, UserDetails currentUserDetails) {
        log.info("Getting user profile with follow stats for user: {}", userEmail);

        User targetUser = getUserByEmail(userEmail);
        FollowStatsDto followStats = getFollowStats(userEmail, currentUserDetails);

        UserProfileWithFollowDto profile = UserProfileWithFollowDto.fromUserAndStats(targetUser, followStats);

        log.info("Successfully retrieved user profile with follow stats for user: {}", userEmail);
        return profile;
    }
}