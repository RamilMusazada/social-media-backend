package org.example.socialmediabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediabackend.model.Post;
import org.example.socialmediabackend.model.PostReaction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String caption;
    private String place;
    private UserProfileDto author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likesCount;
    private long dislikesCount;
    private PostReaction.ReactionType userReaction;

    public static PostResponseDto fromPost(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .place(post.getPlace())
                .author(UserProfileDto.fromUser(post.getAuthor()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDto fromPostWithReactions(
            Post post,
            long likesCount,
            long dislikesCount,
            PostReaction.ReactionType userReaction) {
        return PostResponseDto.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .place(post.getPlace())
                .author(UserProfileDto.fromUser(post.getAuthor()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .userReaction(userReaction)
                .build();
    }
}