package edu.cmipt.gcs.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "User Sign In Data Transfer Object")
public record UserSignInDTO(
        @Schema(
                        description = "Username",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin")
                @NotBlank(message = "{UserSignInDTO.username.NotBlank}")
                String username,
        @Schema(
                        description = "User Password (Unencrypted)",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "123456")
                @NotBlank(message = "{UserSignInDTO.password.NotBlank}")
                String userPassword) {}
