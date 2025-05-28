package org.example.socialmediabackend.controller;

import org.example.socialmediabackend.dto.*;
import org.example.socialmediabackend.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            followService.followUser(followDto.getUserEmail(), userDetails);
            return ResponseEntity.ok(ApiResponse.success("User followed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unfollow")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            followService.unfollowUser(followDto.getUserEmail(), userDetails);
            return ResponseEntity.ok(ApiResponse.success("User unfollowed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats/{email}")
    public ResponseEntity<ApiResponse<FollowStatsDto>> getFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            FollowStatsDto stats = followService.getFollowStats(email, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Follow stats retrieved successfully", stats));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/followers/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> getUserFollowers(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PaginatedResponseDto<UserProfileDto> followers = followService.getFollowers(email, page, size, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Followers retrieved successfully", followers));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/following/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> getUserFollowing(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PaginatedResponseDto<UserProfileDto> following = followService.getFollowing(email, page, size, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Following list retrieved successfully", following));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<ApiResponse<UserProfileWithFollowDto>> getUserProfileWithFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserProfileWithFollowDto profile = followService.getUserProfileWithFollowStats(email, userDetails);
            return ResponseEntity.ok(ApiResponse.success("User profile with follow stats retrieved successfully", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}