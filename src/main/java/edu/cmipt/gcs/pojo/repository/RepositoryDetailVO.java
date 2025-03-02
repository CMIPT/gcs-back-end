package edu.cmipt.gcs.pojo.repository;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryDetailVO(
        @Schema(description = "Repository VO") RepositoryVO repositoryVO,
        @Schema(description = "Branch List") List<String> branchList,
        @Schema(description = "Tag List") List<String> tagList,
        @Schema(description = "Default ref") String defaultRef,
        @Schema(description = "Root Directory Info") List<RepositoryFileVO> rootDirectory
) {
}
