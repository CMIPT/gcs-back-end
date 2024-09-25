package edu.cmipt.gcs.pojo.ssh;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

@Schema(description = "SSH Key Data Transfer Object")
public record SshKeyDTO(
        @Schema(description = "SSH Key ID")
                @Null(groups = CreateGroup.class, message = "SSHKEYDTO_ID_NULL {SshKeyDTO.id.Null}")
                @NotNull(
                        groups = UpdateGroup.class,
                        message = "SSHKEYDTO_ID_NOTNULL {SshKeyDTO.id.NotNull}")
                String id,
        @Schema(description = "Name", example = "My SSH Key")
                @NotBlank(
                        groups = {CreateGroup.class, UpdateGroup.class},
                        message = "SSHKEYDTO_NAME_NOTBLANK {SshKeyDTO.name.NotBlank}")
                @Size(
                        groups = {CreateGroup.class, UpdateGroup.class},
                        min = ValidationConstant.MIN_SSH_KEY_NAME_LENGTH,
                        max = ValidationConstant.MAX_SSH_KEY_NAME_LENGTH,
                        message = "SSHKEYDTO_NAME_SIZE {SshKeyDTO.name.Size}")
                String name,
        @Schema(description = "Public Key")
                @NotBlank(
                        groups = CreateGroup.class,
                        message = "SSHKEYDTO_PUBLICKEY_NOTBLANK {SshKeyDTO.publicKey.NotBlank}")
                @Size(
                        groups = {CreateGroup.class, UpdateGroup.class},
                        min = ValidationConstant.MIN_SSH_KEY_PUBLICKEY_LENGTH,
                        max = ValidationConstant.MAX_SSH_KEY_PUBLICKEY_LENGTH,
                        message = "SSHKEYDTO_PUBLICKEY_SIZE {SshKeyDTO.publicKey.Size}")
                String publicKey) {}
