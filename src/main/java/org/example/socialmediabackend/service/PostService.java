package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.CreatePostDto;
import org.example.socialmediabackend.dto.PaginatedResponseDto;
import org.example.socialmediabackend.dto.PostResponseDto;
import org.example.socialmediabackend.dto.UpdatePostDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.exception.UnauthorizedAccessException;
import org.example.socialmediabackend.model.Post;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.PostRepository;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDto createPost(CreatePostDto createPostDto, UserDetails userDetails) {
        log.info("Creating new post");

        User author = getUserFromUserDetails(userDetails);

        Post post = Post.builder()
                .caption(createPostDto.getCaption())
                .place(createPostDto.getPlace())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);
        PostResponseDto response = PostResponseDto.fromPost(savedPost);

        log.info("Successfully created post with ID: {} by user: {}", savedPost.getId(), author.getEmail());
        return response;
    }

    public PostResponseDto getPostById(Long postId) {
        log.info("Getting post by ID: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        PostResponseDto response = PostResponseDto.fromPost(post);
        log.info("Successfully retrieved post with ID: {}", postId);
        return response;
    }

    public PaginatedResponseDto<PostResponseDto> getAllPosts(int page, int size) {
        log.info("Getting all posts (page: {}, size: {})", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        PaginatedResponseDto<PostResponseDto> response = new PaginatedResponseDto<>(
                postDtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );

        log.info("Successfully retrieved {} posts (total: {})", postDtos.size(), postPage.getTotalElements());
        return response;
    }

    public PaginatedResponseDto<PostResponseDto> getPostsByUser(String userEmail, int page, int size) {
        log.info("Getting posts by user: {} (page: {}, size: {})", userEmail, page, size);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);

        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        PaginatedResponseDto<PostResponseDto> response = new PaginatedResponseDto<>(
                postDtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );

        log.info("Successfully retrieved {} posts for user: {} (total: {})",
                postDtos.size(), userEmail, postPage.getTotalElements());
        return response;
    }

    public PaginatedResponseDto<PostResponseDto> getFeedPosts(UserDetails userDetails, int page, int size) {
        log.info("Getting feed posts (page: {}, size: {})", page, size);

        User currentUser = getUserFromUserDetails(userDetails);
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> feedPosts = postRepository.findPostsFromFollowedUsers(currentUser.getId(), pageable);

        List<PostResponseDto> postDtos = feedPosts.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        PaginatedResponseDto<PostResponseDto> response = new PaginatedResponseDto<>(
                postDtos,
                feedPosts.getNumber(),
                feedPosts.getSize(),
                feedPosts.getTotalElements(),
                feedPosts.getTotalPages(),
                feedPosts.isLast()
        );

        log.info("Successfully retrieved {} feed posts for user: {} (total: {})",
                postDtos.size(), currentUser.getEmail(), feedPosts.getTotalElements());
        return response;
    }

    public PaginatedResponseDto<PostResponseDto> getExplorePosts(UserDetails userDetails, int page, int size) {
        log.info("Getting explore posts (page: {}, size: {})", page, size);

        User currentUser = getUserFromUserDetails(userDetails);
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> explorePosts = postRepository.findPostsExcludingUser(currentUser.getId(), pageable);

        List<PostResponseDto> postDtos = explorePosts.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        PaginatedResponseDto<PostResponseDto> response = new PaginatedResponseDto<>(
                postDtos,
                explorePosts.getNumber(),
                explorePosts.getSize(),
                explorePosts.getTotalElements(),
                explorePosts.getTotalPages(),
                explorePosts.isLast()
        );

        log.info("Successfully retrieved {} explore posts for user: {} (total: {})",
                postDtos.size(), currentUser.getEmail(), explorePosts.getTotalElements());
        return response;
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, UpdatePostDto updatePostDto, UserDetails userDetails) {
        log.info("Updating post with ID: {}", postId);

        User currentUser = getUserFromUserDetails(userDetails);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            log.error("User {} is not authorized to update post with ID: {}", currentUser.getEmail(), postId);
            throw new UnauthorizedAccessException("You are not authorized to update this post");
        }

        if (updatePostDto.getCaption() != null) {
            post.setCaption(updatePostDto.getCaption());
        }

        if (updatePostDto.getPlace() != null) {
            post.setPlace(updatePostDto.getPlace());
        }

        Post updatedPost = postRepository.save(post);
        PostResponseDto response = PostResponseDto.fromPost(updatedPost);

        log.info("Successfully updated post with ID: {} by user: {}", updatedPost.getId(), currentUser.getEmail());
        return response;
    }

    @Transactional
    public void deletePost(Long postId, UserDetails userDetails) {
        log.info("Deleting post with ID: {}", postId);

        User currentUser = getUserFromUserDetails(userDetails);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            log.error("User {} is not authorized to delete post with ID: {}", currentUser.getEmail(), postId);
            throw new UnauthorizedAccessException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        log.info("Successfully deleted post with ID: {} by user: {}", postId, currentUser.getEmail());
    }

    public boolean isPostOwner(Long postId, UserDetails userDetails) {
        log.info("Checking post ownership for post ID: {}", postId);

        User currentUser = getUserFromUserDetails(userDetails);
        boolean isOwner = postRepository.existsByIdAndAuthor(postId, currentUser);

        log.info("Post ownership check result for post ID {} and user {}: {}",
                postId, currentUser.getEmail(), isOwner);
        return isOwner;
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        log.info("Getting user from UserDetails");

        if (userDetails == null) {
            log.error("User not authenticated - UserDetails is null");
            throw new UnauthorizedAccessException("User not authenticated");
        }

        String email = userDetails.getUsername();
        log.info("Looking up user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Successfully retrieved user with email: {}", email);
        return user;
    }
}