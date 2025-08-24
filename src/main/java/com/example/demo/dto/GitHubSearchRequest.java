package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubSearchRequest {
    @NotBlank
    private String query;
    private String language;
    private String sort = "stars";
}