package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;

public record DiffVO(
    @Schema(description = "Old file path. null means new file") String oldPath,
    @Schema(
            description =
                "New file path. null means the file is deleted; when it is equal to oldPath, it"
                    + " means file is updated; otherwise it means file is renamed")
        String newPath,
    @Schema(description = "Diff content") String content) {}
