package org.example.socialmediabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediabackend.model.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileWithFollowDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String profilePictureUrl;
    private FollowStatsDto followStats;

    public static UserProfileWithFollowDto fromUserAndStats(User user, FollowStatsDto followStats) {
        return UserProfileWithFollowDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .followStats(followStats)
                .build();
    }
}