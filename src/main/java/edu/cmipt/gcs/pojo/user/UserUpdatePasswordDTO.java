package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User Update Password Data Transfer Object")
public record UserUpdatePasswordDTO(
    @Schema(description = "Old Password")
        @Size(
            min = ValidationConstant.MIN_PASSWORD_LENGTH,
            max = ValidationConstant.MAX_PASSWORD_LENGTH)
        @NotBlank
        @Pattern(regexp = ValidationConstant.PASSWORD_PATTERN)
        String oldPassword,
    @Schema(description = "New Password")
        @Size(
            min = ValidationConstant.MIN_PASSWORD_LENGTH,
            max = ValidationConstant.MAX_PASSWORD_LENGTH)
        @NotBlank
        @Pattern(regexp = ValidationConstant.PASSWORD_PATTERN)
        String newPassword) {}
