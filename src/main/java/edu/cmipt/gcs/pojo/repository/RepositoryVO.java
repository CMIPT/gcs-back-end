package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryVO(
        @Schema(description = "Repository ID") String id,
        @Schema(description = "Repository Name") String repositoryName,
        @Schema(description = "Repository Description") String repositoryDescription,
        @Schema(description = "Whether or Not Private Repo") Boolean isPrivate,
        @Schema(description = "Owner ID") String userId,
        @Schema(description = "Owner name") String username,
        @Schema(description = "Avatar url") String avatarUrl,
        @Schema(description = "Star Count") Integer star,
        @Schema(description = "Fork Count") Integer fork,
        @Schema(description = "Watcher Count") Integer watcher,
        @Schema(description = "HTTPS URL") String httpsUrl,
        @Schema(description = "SSH URL") String sshUrl) {
    public RepositoryVO(RepositoryPO repositoryPO, String username, String avatarUrl) {
        this(
                repositoryPO.getId().toString(),
                repositoryPO.getRepositoryName(),
                repositoryPO.getRepositoryDescription(),
                repositoryPO.getIsPrivate(),
                repositoryPO.getUserId().toString(),
                username,
                avatarUrl,
                repositoryPO.getStar(),
                repositoryPO.getFork(),
                repositoryPO.getWatcher(),
                repositoryPO.getHttpsUrl(),
                repositoryPO.getSshUrl());
    }
}
