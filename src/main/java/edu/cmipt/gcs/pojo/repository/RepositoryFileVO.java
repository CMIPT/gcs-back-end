package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryFileVO(
        @Schema(description = "File or Directory Path, relative to the request path") String path,
        @Schema(description = "Is file or directory") Boolean isDirectory
) {
}
