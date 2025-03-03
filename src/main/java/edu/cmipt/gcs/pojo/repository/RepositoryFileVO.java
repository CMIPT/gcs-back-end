package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryFileVO(
        @Schema(description = "File or Directory name") String name,
        @Schema(description = "Is file or directory") Boolean isDirectory
) {
}
