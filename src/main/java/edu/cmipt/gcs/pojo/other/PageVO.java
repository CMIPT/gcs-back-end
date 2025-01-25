package edu.cmipt.gcs.pojo.other;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Page Value Object")
public record PageVO<T> (
    @Schema(description = "Total Pages")
    Long pages,
    @Schema(description = "Records for Current Page")
    List<T> records
){}
