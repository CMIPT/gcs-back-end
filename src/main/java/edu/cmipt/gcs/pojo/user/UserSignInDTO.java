package edu.cmipt.gcs.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Sign In Data Transfer Object")
public record UserSignInDTO(
        @Schema(
                        description = "Username",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        example = "admin")
                String username,
        @Schema(
                        description = "User Email",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        example = "user@example.com")
                String email,
        @Schema(
                        description = "User Password (Unencrypted)",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "123456")
                String userPassword) {}
