package edu.cmipt.gcs.pojo.error;

import edu.cmipt.gcs.constant.ErrorMessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorVO(
        @Schema(description = "Error message", example = ErrorMessageConstant.USERNAME_ALREADY_EXISTS) String message) {
}
