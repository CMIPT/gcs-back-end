package edu.cmipt.gcs.pojo.error;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorVO(
    @Schema(description = "Error code") Integer code,
    @Schema(description = "Error message") String message) {
    public ErrorVO(ErrorCodeEnum errorCodeEnum, String message) {
        this(errorCodeEnum.ordinal(), message);
    }
}
