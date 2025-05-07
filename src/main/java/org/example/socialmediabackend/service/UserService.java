package org.example.socialmediabackend.service;

import org.example.socialmediabackend.dto.UpdateProfileRequest;
import org.example.socialmediabackend.dto.UserProfileDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.exception.UnauthorizedAccessException;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserProfileDto.fromUser(user);
    }

    public UserProfileDto getCurrentUserProfile(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("User not authenticated");
        }
        User user = getUserFromUserDetails(userDetails);
        return UserProfileDto.fromUser(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(UpdateProfileRequest updateRequest, UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("User not authenticated");
        }

        User user = getUserFromUserDetails(userDetails);

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isEmpty()) {
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isEmpty()) {
            user.setFirstName(updateRequest.getFirstName());
        }

        if (updateRequest.getLastName() != null && !updateRequest.getLastName().isEmpty()) {
            user.setLastName(updateRequest.getLastName());
        }

        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }

        if (updateRequest.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updateRequest.getProfilePictureUrl());
        }

        User updatedUser = userRepository.save(user);
        return UserProfileDto.fromUser(updatedUser);
    }

    @Transactional
    public void deleteUserAccount(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("User not authenticated");
        }

        User user = getUserFromUserDetails(userDetails);
        userRepository.delete(user);
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
