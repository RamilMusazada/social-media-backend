package org.example.socialmediabackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class FeedController {

    private final PostService postService;

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getFeedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get feed posts API is called, user: {}, page: {}, size: {}", userDetails.getUsername(), page, size);
        PaginatedResponseDto<PostResponseDto> feedPosts = postService.getFeedPosts(userDetails, page, size);
        log.info("Feed posts retrieved successfully, user: {}, count: {}", userDetails.getUsername(), feedPosts.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Feed posts retrieved successfully", feedPosts));
    }

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getExplorePosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get explore posts API is called, user: {}, page: {}, size: {}", userDetails.getUsername(), page, size);
        PaginatedResponseDto<PostResponseDto> explorePosts = postService.getExplorePosts(userDetails, page, size);
        log.info("Explore posts retrieved successfully, user: {}, count: {}", userDetails.getUsername(), explorePosts.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Explore posts retrieved successfully", explorePosts));
    }
}