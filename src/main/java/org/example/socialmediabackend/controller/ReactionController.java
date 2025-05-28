package org.example.socialmediabackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.ReactionRequestDto;
import org.example.socialmediabackend.dto.ReactionResponseDto;
import org.example.socialmediabackend.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReactionResponseDto>> reactToPost(
            @RequestBody ReactionRequestDto reactionRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("React to post API is called, user: {}, request: {}", userDetails.getUsername(), reactionRequestDto);
        ReactionResponseDto response = reactionService.reactToPost(reactionRequestDto, userDetails);
        log.info("Reaction added successfully, postId: {}, user: {}, reactionType: {}",
                reactionRequestDto.getPostId(), userDetails.getUsername(), reactionRequestDto.getReactionType());
        return ResponseEntity.ok(ApiResponse.success("Reaction added successfully", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<ReactionResponseDto>> removeReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Remove reaction API is called, postId: {}, user: {}", postId, userDetails.getUsername());
        ReactionResponseDto response = reactionService.removeReaction(postId, userDetails);
        log.info("Reaction removed successfully, postId: {}, user: {}", postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<ReactionResponseDto>> getPostReactionDetails(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get post reaction details API is called, postId: {}, user: {}", postId, userDetails.getUsername());
        ReactionResponseDto response = reactionService.getPostReactionDetails(postId, userDetails);
        log.info("Post reaction details retrieved successfully, postId: {}", postId);
        return ResponseEntity.ok(ApiResponse.success("Post reaction details retrieved successfully", response));
    }
}