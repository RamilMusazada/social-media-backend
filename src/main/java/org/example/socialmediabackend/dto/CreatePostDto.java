package org.example.socialmediabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {
    @NotBlank(message = "Caption is required")
    @Size(max = 1000, message = "Caption cannot exceed 1000 characters")
    private String caption;

    @Size(max = 100, message = "Place cannot exceed 100 characters")
    private String place;
}