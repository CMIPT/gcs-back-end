package edu.cmipt.gcs.pojo.repository;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RepositoryFileDetailVO(
    @Schema(description = "Is file or directory") Boolean isDirectory,
    @Schema(description = "When the path is a file, this will be the content of the file")
        String content,
    @Schema(
            description =
                "When the path is a directory and there is a READM.md, this will be"
                    + " the content of the file")
        String readmeContent,
    @Schema(
            description =
                "When the path is a directory and there is a LICENSE, this will be"
                    + " the content of the file")
        String licenseContent,
    @Schema(description = "When the path is a directory, this contains the sub-dir or files")
        List<RepositoryFileVO> directoryList) {}
