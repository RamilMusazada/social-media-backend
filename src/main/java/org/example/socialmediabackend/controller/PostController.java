package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.*;
import org.example.socialmediabackend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Valid @RequestBody CreatePostDto createPostDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Create post API is called, user: {}, request: {}", userDetails.getUsername(), createPostDto);
        PostResponseDto createdPost = postService.createPost(createPostDto, userDetails);
        log.info("Post created successfully, id: {}, user: {}", createdPost.getId(), userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Post created successfully", createdPost),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(@PathVariable Long postId) {
        log.info("Get post by ID API is called, postId: {}", postId);
        PostResponseDto post = postService.getPostById(postId);
        log.info("Post retrieved successfully, id: {}", postId);
        return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", post));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all posts API is called, page: {}, size: {}", page, size);
        PaginatedResponseDto<PostResponseDto> posts = postService.getAllPosts(page, size);
        log.info("All posts retrieved successfully, count: {}", posts.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", posts));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getPostsByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get posts by user API is called, user: {}, page: {}, size: {}", email, page, size);
        PaginatedResponseDto<PostResponseDto> posts = postService.getPostsByUser(email, page, size);
        log.info("User posts retrieved successfully, user: {}, count: {}", email, posts.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("User posts retrieved successfully", posts));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostDto updatePostDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Update post API is called, postId: {}, user: {}, request: {}", postId, userDetails.getUsername(), updatePostDto);
        PostResponseDto updatedPost = postService.updatePost(postId, updatePostDto, userDetails);
        log.info("Post updated successfully, id: {}, user: {}", postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", updatedPost));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Delete post API is called, postId: {}, user: {}", postId, userDetails.getUsername());
        postService.deletePost(postId, userDetails);
        log.info("Post deleted successfully, id: {}, user: {}", postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @GetMapping("/{postId}/check-owner")
    public ResponseEntity<ApiResponse<Boolean>> isPostOwner(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Check post owner API is called, postId: {}, user: {}", postId, userDetails.getUsername());
        boolean isOwner = postService.isPostOwner(postId, userDetails);
        log.info("Post ownership check completed, postId: {}, user: {}, isOwner: {}", postId, userDetails.getUsername(), isOwner);
        return ResponseEntity.ok(ApiResponse.success("Ownership check completed", isOwner));
    }
}