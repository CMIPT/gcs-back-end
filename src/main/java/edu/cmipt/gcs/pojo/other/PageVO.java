package edu.cmipt.gcs.pojo.other;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Page Value Object")
public record PageVO<T>(
    @Schema(description = "Total Records") Long total,
    @Schema(description = "Records for Current Page") List<T> records) {}
