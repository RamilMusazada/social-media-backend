package org.example.socialmediabackend.controller;

import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.ReactionRequestDto;
import org.example.socialmediabackend.dto.ReactionResponseDto;
import org.example.socialmediabackend.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReactionResponseDto>> reactToPost(
            @RequestBody ReactionRequestDto reactionRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReactionResponseDto response = reactionService.reactToPost(reactionRequestDto, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Reaction added successfully", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<ReactionResponseDto>> removeReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReactionResponseDto response = reactionService.removeReaction(postId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<ReactionResponseDto>> getPostReactionDetails(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReactionResponseDto response = reactionService.getPostReactionDetails(postId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Post reaction details retrieved successfully", response));
    }
}
