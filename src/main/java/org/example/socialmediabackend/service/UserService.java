package org.example.socialmediabackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.UpdateProfileRequest;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.exception.UnauthorizedAccessException;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public List<User> allUsers() {
        log.info("Getting all users");

        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);

        log.info("Successfully retrieved {} users", users.size());
        return users;
    }

    public UserProfileDto getUserProfileByEmail(String email) {
        log.info("Getting user profile by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserProfileDto profile = UserProfileDto.fromUser(user);
        log.info("Successfully retrieved user profile for email: {}", email);
        return profile;
    }

    public UserProfileDto getCurrentUserProfile(UserDetails userDetails) {
        log.info("Getting current user profile");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new UnauthorizedAccessException("User not authenticated");
        }

        User user = getUserFromUserDetails(userDetails);
        UserProfileDto profile = UserProfileDto.fromUser(user);

        log.info("Successfully retrieved current user profile for user: {}", user.getEmail());
        return profile;
    }

    @Transactional
    public UserProfileDto updateUserProfile(UpdateProfileRequest updateRequest, UserDetails userDetails) {
        log.info("Updating user profile");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new UnauthorizedAccessException("User not authenticated");
        }

        User user = getUserFromUserDetails(userDetails);

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isEmpty()) {
            user.setUsername(updateRequest.getUsername());
            log.info("Updated username for user: {}", user.getEmail());
        }

        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isEmpty()) {
            user.setFirstName(updateRequest.getFirstName());
            log.info("Updated first name for user: {}", user.getEmail());
        }

        if (updateRequest.getLastName() != null && !updateRequest.getLastName().isEmpty()) {
            user.setLastName(updateRequest.getLastName());
            log.info("Updated last name for user: {}", user.getEmail());
        }

        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
            log.info("Updated bio for user: {}", user.getEmail());
        }

        if (updateRequest.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updateRequest.getProfilePictureUrl());
            log.info("Updated profile picture URL for user: {}", user.getEmail());
        }

        User updatedUser = userRepository.save(user);
        UserProfileDto profile = UserProfileDto.fromUser(updatedUser);

        log.info("Successfully updated user profile for user: {}", updatedUser.getEmail());
        return profile;
    }

    @Transactional
    public void deleteUserAccount(UserDetails userDetails) {
        log.info("Deleting user account");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new UnauthorizedAccessException("User not authenticated");
        }

        User user = getUserFromUserDetails(userDetails);
        userRepository.delete(user);

        log.info("Successfully deleted user account for user: {}", user.getEmail());
    }

    public PaginatedResponseDto<UserProfileDto> searchUsers(String firstName, String lastName, String username, Integer page, Integer size) {
        log.info("Searching users with criteria - firstName: {}, lastName: {}, username: {}, page: {}, size: {}",
                firstName, lastName, username, page, size);

        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> userPage = userRepository.searchUsers(firstName, lastName, username, pageable);

        List<UserProfileDto> userDtos = userPage.getContent().stream()
                .map(UserProfileDto::fromUser)
                .collect(Collectors.toList());

        PaginatedResponseDto<UserProfileDto> response = new PaginatedResponseDto<>(
                userDtos,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );

        log.info("Successfully found {} users matching search criteria (total: {})",
                userDtos.size(), userPage.getTotalElements());
        return response;
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        log.info("Getting user from UserDetails");

        String email = userDetails.getUsername();
        log.info("Looking up user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Successfully retrieved user with email: {}", email);
        return user;
    }
}