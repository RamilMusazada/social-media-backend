package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import org.example.socialmediabackend.dto.*;
import org.example.socialmediabackend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Valid @RequestBody CreatePostDto createPostDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PostResponseDto createdPost = postService.createPost(createPostDto, userDetails);
            return new ResponseEntity<>(
                    ApiResponse.success("Post created successfully", createdPost),
                    HttpStatus.CREATED
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(@PathVariable Long postId) {
        try {
            PostResponseDto post = postService.getPostById(postId);
            return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", post));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PaginatedResponseDto<PostResponseDto> posts = postService.getAllPosts(page, size);
            return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", posts));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<PaginatedResponseDto<PostResponseDto>>> getPostsByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PaginatedResponseDto<PostResponseDto> posts = postService.getPostsByUser(email, page, size);
            return ResponseEntity.ok(ApiResponse.success("User posts retrieved successfully", posts));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostDto updatePostDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PostResponseDto updatedPost = postService.updatePost(postId, updatePostDto, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Post updated successfully", updatedPost));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            postService.deletePost(postId, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{postId}/check-owner")
    public ResponseEntity<ApiResponse<Boolean>> isPostOwner(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boolean isOwner = postService.isPostOwner(postId, userDetails);
            return ResponseEntity.ok(ApiResponse.success("Ownership check completed", isOwner));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}