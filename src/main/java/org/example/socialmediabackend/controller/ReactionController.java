package org.example.socialmediabackend.controller;

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
    public ResponseEntity<ReactionResponseDto> reactToPost(
            @RequestBody ReactionRequestDto reactionRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReactionResponseDto response = reactionService.reactToPost(reactionRequestDto, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ReactionResponseDto> removeReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReactionResponseDto response = reactionService.removeReaction(postId, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ReactionResponseDto> getPostReactionDetails(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReactionResponseDto response = reactionService.getPostReactionDetails(postId, userDetails);
        return ResponseEntity.ok(response);
    }
}
