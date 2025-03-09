package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryVO(
    @Schema(description = "Repository ID") String id,
    @Schema(description = "Repository Name") String repositoryName,
    @Schema(description = "Repository Description") String repositoryDescription,
    @Schema(description = "Whether or Not Private Repo") Boolean isPrivate,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Owner name") String username,
    @Schema(description = "Owner's avatar url") String avatarUrl,
    @Schema(description = "Star Count") Integer star,
    @Schema(description = "Fork Count") Integer fork,
    @Schema(description = "Watcher Count") Integer watcher) {
  public RepositoryVO(RepositoryPO repositoryPO, String username, String avatarUrl) {
    this(
        repositoryPO.getId().toString(),
        repositoryPO.getRepositoryName(),
        repositoryPO.getRepositoryDescription(),
        repositoryPO.getIsPrivate(),
        String.valueOf(repositoryPO.getGmtCreated().getTime()),
        username,
        avatarUrl,
        repositoryPO.getStar(),
        repositoryPO.getFork(),
        repositoryPO.getWatcher());
  }
}
