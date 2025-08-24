package com.example.demo.controller;

import com.example.demo.dto.GitHubSearchRequest;
import com.example.demo.dto.GitHubRepositoryResponse;
import com.example.demo.dto.ResponseMessage;
import com.example.demo.entity.GitHubRepository;
import com.example.demo.repository.GitHubRepositoryRepository;
import com.example.demo.service.GitHubSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubSearchService searchService;
    private final GitHubRepositoryRepository repository;

    @PostMapping("/search")
    public ResponseMessage searchRepositories(@Valid @RequestBody GitHubSearchRequest request) {
        List<GitHubRepository> repos = searchService.searchAndSaveRepositories(request);
        return new ResponseMessage("Repositories fetched and saved successfully", mapToResponseList(repos));
    }

    @GetMapping("/repositories")
    public List<GitHubRepositoryResponse> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "0") int minStars,
            @RequestParam(required = false, defaultValue = "stars") String sort) {

        List<GitHubRepository> repos = repository.findAll().stream()
                .filter(r -> language == null || (r.getLanguage() != null && r.getLanguage().equalsIgnoreCase(language)))
                .filter(r -> r.getStars() >= minStars)
                .sorted((a, b) -> {
                    switch (sort) {
                        case "forks":
                            return Integer.compare(b.getForks(), a.getForks());
                        case "updated":
                            return b.getLastUpdated().compareTo(a.getLastUpdated());
                        case "stars":
                        default:
                            return Integer.compare(b.getStars(), a.getStars());
                    }
                })
                .collect(Collectors.toList());

        return mapToResponseList(repos);
    }

    private List<GitHubRepositoryResponse> mapToResponseList(List<GitHubRepository> repos) {
        return repos.stream()
                .map(repo -> GitHubRepositoryResponse.builder()
                        .id(repo.getId())
                        .name(repo.getName())
                        .description(repo.getDescription())
                        .owner(repo.getOwner())
                        .language(repo.getLanguage())
                        .stars(repo.getStars())
                        .forks(repo.getForks())
                        .lastUpdated(repo.getLastUpdated())
                        .build())
                .collect(Collectors.toList());
    }
}