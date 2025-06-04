package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CommitDetailVO(
    @Schema(description = "Commit hash") String hash,
    @Schema(description = "Commit message") String message,
    @Schema(description = "Commit timestamp") String timestamp,
    @Schema(description = "Author of the commit") CommitAuthorVO author,
    @Schema(description = "Diff information for updated files") List<DiffVO> diffList) {}
