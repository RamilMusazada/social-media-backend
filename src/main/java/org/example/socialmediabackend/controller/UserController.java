package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.UpdateProfileRequest;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get current user profile API is called, user: {}", userDetails.getUsername());
        UserProfileDto profile = userService.getCurrentUserProfile(userDetails);
        log.info("Current user profile retrieved successfully, user: {}", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@PathVariable String email) {
        log.info("Get user profile API is called, email: {}", email);
        UserProfileDto profile = userService.getUserProfileByEmail(email);
        log.info("User profile retrieved successfully, email: {}", email);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Search users API is called, firstName: {}, lastName: {}, username: {}, page: {}, size: {}",
                firstName, lastName, username, page, size);
        PaginatedResponseDto<UserProfileDto> searchResults =
                userService.searchUsers(firstName, lastName, username, page, size);
        log.info("User search completed successfully, count: {}", searchResults.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("User search completed successfully", searchResults));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Update user profile API is called, user: {}, request: {}", userDetails.getUsername(), updateRequest);
        UserProfileDto updatedProfile = userService.updateUserProfile(updateRequest, userDetails);
        log.info("Profile updated successfully, user: {}", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Delete user account API is called, user: {}", userDetails.getUsername());
        userService.deleteUserAccount(userDetails);
        log.info("User account successfully deleted, user: {}", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User account successfully deleted"));
    }
}