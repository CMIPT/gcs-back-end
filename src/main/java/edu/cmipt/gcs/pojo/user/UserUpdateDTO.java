package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.constant.ValidationConstant;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User Data Transfer Object
 *
 * @author Kaiser
 */
@Schema(description = "User Update Data Transfer Object")
public record UserUpdateDTO(
        @Schema(description = "User ID")
                @NotNull
                // The Long can not be expressed correctly in json, so use String instead
                String id,
        @Schema(
                        description = "Username",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin")
                @Size(
                        min = ValidationConstant.MIN_USERNAME_LENGTH,
                        max = ValidationConstant.MAX_USERNAME_LENGTH)
                @NotBlank
                @Pattern(
                        regexp = ValidationConstant.USERNAME_PATTERN
                        )
                String username) {}
