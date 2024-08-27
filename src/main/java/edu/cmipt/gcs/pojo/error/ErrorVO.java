package edu.cmipt.gcs.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorVO(
        @Schema(description = "Error message") String message) {
}
