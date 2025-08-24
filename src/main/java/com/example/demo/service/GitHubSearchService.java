package com.example.demo.service;

import com.example.demo.dto.GitHubSearchRequest;
import com.example.demo.entity.GitHubRepository;
import com.example.demo.repository.GitHubRepositoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubSearchService {

    private final GitHubRepositoryRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<GitHubRepository> searchAndSaveRepositories(GitHubSearchRequest request) {
        String url = "https://api.github.com/search/repositories?q=" + request.getQuery();

        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            url += "+language:" + request.getLanguage();
        }

        url += "&sort=" + request.getSort();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling GitHub API: " + e.getMessage(), e);
        }

        List<GitHubRepository> savedRepos = new ArrayList<>();

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode items = root.path("items");

                if (items.isEmpty()) {
                    return savedRepos;
                }

                for (JsonNode item : items) {
                    GitHubRepository repo = GitHubRepository.builder()
                        .id(item.path("id").asLong())
                        .name(item.path("name").asText())
                        .description(item.path("description").asText(null))
                        .owner(item.path("owner").path("login").asText())
                        .language(item.path("language").asText(null))
                        .stars(item.path("stargazers_count").asInt())
                        .forks(item.path("forks_count").asInt())
                        .lastUpdated(ZonedDateTime.parse(item.path("updated_at").asText()))
                        .build();

                    repository.save(repo);
                    savedRepos.add(repo);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing GitHub response", e);
            }
        } else {
            throw new RuntimeException("GitHub API failed with status: " + response.getStatusCode());
        }

        return savedRepos;
    }
}