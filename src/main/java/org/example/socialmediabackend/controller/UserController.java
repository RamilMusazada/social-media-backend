package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.UpdateProfileRequest;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserProfileDto profile = userService.getCurrentUserProfile(userDetails);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@PathVariable String email) {
        try {
            UserProfileDto profile = userService.getUserProfileByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<UserProfileDto>>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            PaginatedResponseDto<UserProfileDto> searchResults =
                    userService.searchUsers(firstName, lastName, username, page, size);
            return ResponseEntity.ok(ApiResponse.success("User search completed successfully", searchResults));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserProfileDto updatedProfile = userService.updateUserProfile(updateRequest, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.deleteUserAccount(userDetails);
            return ResponseEntity.ok(ApiResponse.success("User account successfully deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}