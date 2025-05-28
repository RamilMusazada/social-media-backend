package org.example.socialmediabackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.*;
import org.example.socialmediabackend.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Follow user API is called, follower: {}, target: {}", userDetails.getUsername(), followDto.getUserEmail());
        followService.followUser(followDto.getUserEmail(), userDetails);
        log.info("User followed successfully, follower: {}, target: {}", userDetails.getUsername(), followDto.getUserEmail());
        return ResponseEntity.ok(ApiResponse.success("User followed successfully"));
    }

    @PostMapping("/unfollow")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Unfollow user API is called, unfollower: {}, target: {}", userDetails.getUsername(), followDto.getUserEmail());
        followService.unfollowUser(followDto.getUserEmail(), userDetails);
        log.info("User unfollowed successfully, unfollower: {}, target: {}", userDetails.getUsername(), followDto.getUserEmail());
        return ResponseEntity.ok(ApiResponse.success("User unfollowed successfully"));
    }

    @GetMapping("/stats/{email}")
    public ResponseEntity<ApiResponse<FollowStatsDto>> getFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get follow stats API is called, requester: {}, target: {}", userDetails.getUsername(), email);
        FollowStatsDto stats = followService.getFollowStats(email, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Follow stats retrieved successfully", stats));
    }

    @GetMapping("/followers/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> getUserFollowers(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get user followers API is called, requester: {}, target: {}, page: {}, size: {}", userDetails.getUsername(), email, page, size);
        PaginatedResponseDto<UserProfileDto> followers = followService.getFollowers(email, page, size, userDetails);
        log.info("Followers retrieved successfully, target: {}, count: {}", email, followers.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Followers retrieved successfully", followers));
    }

    @GetMapping("/following/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> getUserFollowing(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get user following API is called, requester: {}, target: {}, page: {}, size: {}", userDetails.getUsername(), email, page, size);
        PaginatedResponseDto<UserProfileDto> following = followService.getFollowing(email, page, size, userDetails);
        log.info("Following list retrieved successfully, target: {}, count: {}", email, following.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Following list retrieved successfully", following));
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<ApiResponse<UserProfileWithFollowDto>> getUserProfileWithFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get user profile with follow stats API is called, requester: {}, target: {}", userDetails.getUsername(), email);
        UserProfileWithFollowDto profile = followService.getUserProfileWithFollowStats(email, userDetails);
        log.info("User profile with follow stats retrieved successfully, target: {}", email);
        return ResponseEntity.ok(ApiResponse.success("User profile with follow stats retrieved successfully", profile));
    }
}