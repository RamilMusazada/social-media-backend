package org.example.socialmediabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchRequestDto {
    private String firstName;
    private String lastName;
    private String username;
    private Integer page;
    private Integer size;
}