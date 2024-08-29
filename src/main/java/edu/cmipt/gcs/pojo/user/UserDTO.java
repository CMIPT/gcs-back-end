package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

/**
 * User Data Transfer Object
 *
 * @author Kaiser
 */
@Schema(description = "User Data Transfer Object")
public record UserDTO(
        @Schema(
                        description = "User ID",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        accessMode = Schema.AccessMode.READ_ONLY)
                @Null(groups = CreateGroup.class, message = "USERDTO_ID_NULL {UserDTO.id.Null}")
                @NotNull(
                        groups = UpdateGroup.class,
                        message = "USERDTO_ID_NOTNULL {UserDTO.id.NotNull}")
                Long id,
        @Schema(
                        description = "Username",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin")
                @Size(
                        groups = {CreateGroup.class},
                        min = ValidationConstant.MIN_USERNAME_LENGTH,
                        max = ValidationConstant.MAX_USERNAME_LENGTH,
                        message = "USERDTO_USERNAME_SIZE {UserDTO.username.Size}")
                @NotBlank(
                        groups = {CreateGroup.class},
                        message = "USERDTO_USERNAME_NOTBLANK {UserDTO.username.NotBlank}")
                String username,
        @Schema(
                        description = "Email",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "admin@cmipt.edu")
                @Email(
                        groups = {CreateGroup.class},
                        message = "USERDTO_EMAIL_EMAIL {UserDTO.email.Email}")
                @NotBlank(
                        groups = {CreateGroup.class},
                        message = "USERDTO_EMAIL_NOTBLANK {UserDTO.email.NotBlank}")
                String email,
        @Schema(
                        description = "User Password (Unencrypted)",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "123456")
                @Size(
                        groups = {CreateGroup.class},
                        min = ValidationConstant.MIN_PASSWORD_LENGTH,
                        max = ValidationConstant.MAX_PASSWORD_LENGTH,
                        message = "USERDTO_USERPASSWORD_SIZE {UserDTO.userPassword.Size}")
                @NotBlank(
                        groups = {CreateGroup.class},
                        message = "USERDTO_USERPASSWORD_NOTBLANK {UserDTO.userPassword.NotBlank}")
                String userPassword) {}
