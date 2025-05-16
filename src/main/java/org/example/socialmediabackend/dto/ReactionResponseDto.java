package org.example.socialmediabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediabackend.model.PostReaction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponseDto {
    private Long postId;
    private PostReaction.ReactionType userReaction;
    private long likesCount;
    private long dislikesCount;
}