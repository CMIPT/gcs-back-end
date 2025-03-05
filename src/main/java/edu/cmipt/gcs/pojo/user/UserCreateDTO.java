package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User Sign Up Data Transfer Object")
public record UserCreateDTO(
    @Schema(
            description = "Username",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "admin")
        @Size(
            min = ValidationConstant.MIN_USERNAME_LENGTH,
            max = ValidationConstant.MAX_USERNAME_LENGTH)
        @NotBlank
        @Pattern(regexp = ValidationConstant.USERNAME_PATTERN)
        String username,
    @Schema(
            description = "Email",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "admin@cmipt.edu")
        @Email
        @NotBlank
        String email,
    @Schema(
            description = "User Password (Unencrypted)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "123456")
        @Size(
            min = ValidationConstant.MIN_PASSWORD_LENGTH,
            max = ValidationConstant.MAX_PASSWORD_LENGTH)
        @NotBlank
        @Pattern(regexp = ValidationConstant.PASSWORD_PATTERN)
        String userPassword,
    @Schema(
            description = "Email Verification Code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "123456")
        String emailVerificationCode) {}
