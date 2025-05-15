package org.example.socialmediabackend.service;

import jakarta.transaction.Transactional;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PostService {
    private static final Logger logger = Logger.getLogger(PostService.class.getName());

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("User not authenticated");
        }

        String email = userDetails.getUsername();
        logger.info("Looking up user with email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public PostResponseDto createPost(CreatePostDto createPostDto, UserDetails userDetails) {
        User author = getUserFromUserDetails(userDetails);

        Post post = Post.builder()
                .caption(createPostDto.getCaption())
                .place(createPostDto.getPlace())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);
        logger.info("Post created with ID: " + savedPost.getId() + " by user: " + author.getEmail());

        return PostResponseDto.fromPost(savedPost);
    }

    public PostResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        return PostResponseDto.fromPost(post);
    }

    public PaginatedResponseDto<PostResponseDto> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                postDtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    public PaginatedResponseDto<PostResponseDto> getPostsByUser(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);

        List<PostResponseDto> postDtos = postPage.getContent().stream()
                .map(PostResponseDto::fromPost)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                postDtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, UpdatePostDto updatePostDto, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to update this post");
        }

        if (updatePostDto.getCaption() != null) {
            post.setCaption(updatePostDto.getCaption());
        }

        if (updatePostDto.getPlace() != null) {
            post.setPlace(updatePostDto.getPlace());
        }

        Post updatedPost = postRepository.save(post);
        logger.info("Post updated with ID: " + updatedPost.getId() + " by user: " + currentUser.getEmail());

        return PostResponseDto.fromPost(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        // Check if the current user is the author of the post
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        logger.info("Post deleted with ID: " + postId + " by user: " + currentUser.getEmail());
    }

    public boolean isPostOwner(Long postId, UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);
        return postRepository.existsByIdAndAuthor(postId, currentUser);
    }
}