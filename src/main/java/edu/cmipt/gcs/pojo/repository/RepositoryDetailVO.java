package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RepositoryDetailVO(
    @Schema(description = "Repository ID") String id,
    @Schema(description = "Repository Name") String repositoryName,
    @Schema(description = "Repository Description") String repositoryDescription,
    @Schema(description = "Whether or Not Private Repo") Boolean isPrivate,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Owner's ID") String userId,
    @Schema(description = "Owner name") String username,
    @Schema(description = "Owner's avatar url") String avatarUrl,
    @Schema(description = "Star Count") Integer star,
    @Schema(description = "Fork Count") Integer fork,
    @Schema(description = "Watcher Count") Integer watcher,
    @Schema(description = "HTTPS URL") String httpsUrl,
    @Schema(description = "SSH URL") String sshUrl,
    @Schema(description = "Branch List") List<String> branchList,
    @Schema(description = "Tag List") List<String> tagList,
    @Schema(description = "Default ref") String defaultRef,
    @Schema(description = "Latest commit information") CommitVO commit) {
  public RepositoryDetailVO(
      RepositoryPO repositoryPO,
      String username,
      String avatarUrl,
      List<String> branchList,
      List<String> tagList,
      String defaultRef,
      CommitVO commit) {
    this(
        repositoryPO.getId().toString(),
        repositoryPO.getRepositoryName(),
        repositoryPO.getRepositoryDescription(),
        repositoryPO.getIsPrivate(),
        String.valueOf(repositoryPO.getGmtCreated().getTime()),
        repositoryPO.getUserId().toString(),
        username,
        avatarUrl,
        repositoryPO.getStar(),
        repositoryPO.getFork(),
        repositoryPO.getWatcher(),
        repositoryPO.getHttpsUrl(),
        repositoryPO.getSshUrl(),
        branchList,
        tagList,
        defaultRef,
        commit);
  }
}
