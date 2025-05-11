package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.UpdateProfileRequest;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.service.UserService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getCurrentUserProfile(userDetails);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String email) {
        UserProfileDto profile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponseDto<UserProfileDto>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        PaginatedResponseDto<UserProfileDto> searchResults =
                userService.searchUsers(firstName, lastName, username, page, size);

        return ResponseEntity.ok(searchResults);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto updatedProfile = userService.updateUserProfile(updateRequest, userDetails);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deleteUserAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUserAccount(userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("User account successfully deleted"));
    }
}