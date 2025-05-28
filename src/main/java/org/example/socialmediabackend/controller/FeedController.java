package org.example.socialmediabackend.controller;

import org.example.socialmediabackend.dto.ApiResponse;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.PostResponseDto;
import org.example.socialmediabackend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getFeedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PaginatedResponseDto<PostResponseDto> feedPosts =
                    postService.getFeedPosts(userDetails, page, size);
            return ResponseEntity.ok(ApiResponse.success("Feed posts retrieved successfully", feedPosts));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getExplorePosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PaginatedResponseDto<PostResponseDto> explorePosts =
                    postService.getExplorePosts(userDetails, page, size);
            return ResponseEntity.ok(ApiResponse.success("Explore posts retrieved successfully", explorePosts));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}