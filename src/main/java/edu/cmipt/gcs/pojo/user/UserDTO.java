package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.validation.ConstantProperty;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User Data Transfer Object
 *
 * @author Kaiser
 */
@Schema(description = "User Data Transfer Object")
public record UserDTO(
        @Schema(
                        description = "Username",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin")
                @Size(
                        groups = {UpdateGroup.class, CreateGroup.class},
                        min = ConstantProperty.MIN_USERNAME_LENGTH,
                        max = ConstantProperty.MAX_USERNAME_LENGTH,
                        message = "{UserDTO.username.Size}")
                String username,
        @Schema(
                        description = "Email",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin@cmipt.edu")
                @Email(
                        groups = {UpdateGroup.class, CreateGroup.class},
                        message = "{UserDTO.email.Email}")
                @NotBlank(
                        groups = {UpdateGroup.class, CreateGroup.class},
                        message = "{UserDTO.email.NotBlank}")
                String email,
        @Schema(
                        description = "User Password (Unencrypted)",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "123456")
                @Size(
                        groups = {UpdateGroup.class, CreateGroup.class},
                        min = ConstantProperty.MIN_PASSWORD_LENGTH,
                        max = ConstantProperty.MAX_PASSWORD_LENGTH,
                        message = "{UserDTO.userPassword.Size}")
                String userPassword) {}
