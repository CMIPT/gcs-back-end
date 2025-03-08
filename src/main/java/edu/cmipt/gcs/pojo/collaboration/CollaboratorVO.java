package edu.cmipt.gcs.pojo.collaboration;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Collaborator Value Object")
public record CollaboratorVO(
    @Schema(description = "Collaboration id") String id,
    @Schema(description = "Collaborator id") String collaboratorId,
    @Schema(description = "User's name") String username,
    @Schema(description = "User's email") String email,
    @Schema(description = "User's avatar URL") String avatarUrl) {
  public CollaboratorVO(CollaboratorDTO collaboratorDTO) {
    this(
        collaboratorDTO.getId().toString(),
        collaboratorDTO.getCollaboratorId().toString(),
        collaboratorDTO.getUsername(),
        collaboratorDTO.getEmail(),
        collaboratorDTO.getAvatarUrl());
  }
}
