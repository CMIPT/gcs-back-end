package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryFileVO(
    @Schema(description = "File or Directory name") String name,
    @Schema(description = "Is file or directory") Boolean isDirectory,
    @Schema(description = "The latest commit hash of the file") String commitHash,
    @Schema(description = "The latest commit messeage of the file") String commitMessage,
    @Schema(description = "The latest timestamp of the commit") String commitTimestamp,
    @Schema(description = "The author of the latest commit") CommitAuthorVO commitAuthor
) {}
