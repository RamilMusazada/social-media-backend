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
    public ResponseEntity<ApiResponse> followUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        followService.followUser(followDto.getUserEmail(), userDetails);
        return ResponseEntity.ok(ApiResponse.success("User followed successfully"));
    }

    @PostMapping("/unfollow")
    public ResponseEntity<ApiResponse> unfollowUser(
            @RequestBody FollowDto followDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        followService.unfollowUser(followDto.getUserEmail(), userDetails);
        return ResponseEntity.ok(ApiResponse.success("User unfollowed successfully"));
    }

    @GetMapping("/stats/{email}")
    public ResponseEntity<FollowStatsDto> getFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        FollowStatsDto stats = followService.getFollowStats(email, userDetails);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/followers/{email}")
    public ResponseEntity<PaginatedResponseDto<UserProfileDto>> getUserFollowers(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaginatedResponseDto<UserProfileDto> followers = followService.getFollowers(email, page, size, userDetails);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{email}")
    public ResponseEntity<PaginatedResponseDto<UserProfileDto>> getUserFollowing(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaginatedResponseDto<UserProfileDto> following = followService.getFollowing(email, page, size, userDetails);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<UserProfileWithFollowDto> getUserProfileWithFollowStats(
            @PathVariable String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileWithFollowDto profile = followService.getUserProfileWithFollowStats(email, userDetails);
        return ResponseEntity.ok(profile);
    }
}