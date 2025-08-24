package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "github_repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubRepository {

    @Id
    private Long id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String owner;
    private String language;

    private int stars;
    private int forks;

    private ZonedDateTime lastUpdated;
}