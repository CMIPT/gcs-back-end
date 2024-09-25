package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryVO(
        @Schema(description = "Repository ID") String id,
        @Schema(description = "Repository Name") String repositoryName,
        @Schema(description = "Repository Description") String repositoryDescription,
        @Schema(description = "Whether or Not Private Repo") Boolean isPrivate,
        @Schema(description = "Owner ID") Long userId,
        @Schema(description = "Star Count") Integer star,
        @Schema(description = "Fork Count") Integer fork,
        @Schema(description = "Watcher Count") Integer watcher) {
    public RepositoryVO(RepositoryPO repositoryPO) {
        this(
                repositoryPO.getId().toString(),
                repositoryPO.getRepositoryName(),
                repositoryPO.getRepositoryDescription(),
                repositoryPO.getIsPrivate(),
                repositoryPO.getUserId(),
                repositoryPO.getStar(),
                repositoryPO.getFork(),
                repositoryPO.getWatcher());
    }
}
