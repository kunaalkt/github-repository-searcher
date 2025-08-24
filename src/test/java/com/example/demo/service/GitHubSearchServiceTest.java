package com.example.demo.service;

import com.example.demo.dto.GitHubSearchRequest;
import com.example.demo.entity.GitHubRepository;
import com.example.demo.repository.GitHubRepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubSearchServiceTest {

    @Mock
    private GitHubRepositoryRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GitHubSearchService searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchAndSaveRepositories_successfulResponse_savesRepositories() {
        GitHubSearchRequest request = new GitHubSearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        String mockResponseJson = """
            {
              "items": [
                {
                  "id": 123,
                  "name": "spring-boot-demo",
                  "description": "A Spring Boot demo project",
                  "owner": {
                    "login": "user123"
                  },
                  "language": "Java",
                  "stargazers_count": 100,
                  "forks_count": 20,
                  "updated_at": "2024-01-01T12:00:00Z"
                }
              ]
            }
        """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        when(repository.save(any(GitHubRepository.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<GitHubRepository> results = searchService.searchAndSaveRepositories(request);

        assertEquals(1, results.size());
        GitHubRepository repo = results.get(0);
        assertEquals(123L, repo.getId());
        assertEquals("spring-boot-demo", repo.getName());
        assertEquals("A Spring Boot demo project", repo.getDescription());
        assertEquals("user123", repo.getOwner());
        assertEquals("Java", repo.getLanguage());
        assertEquals(100, repo.getStars());
        assertEquals(20, repo.getForks());
        assertEquals(ZonedDateTime.parse("2024-01-01T12:00:00Z"), repo.getLastUpdated());

        verify(repository, times(1)).save(any(GitHubRepository.class));
    }
}