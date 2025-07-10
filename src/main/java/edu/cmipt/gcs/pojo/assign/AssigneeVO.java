package edu.cmipt.gcs.pojo.assign;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Assignee VO")
public record AssigneeVO(
    // The Long can not be expressed correctly in json, so use String instead
    @Schema(description = "Assignment ID") String id,
    @Schema(description = "Assignee ID") String assigneeId,
    @Schema(description = "Username", example = "admin") String username,
    @Schema(description = "Email", example = "admin@cmipt.edu") String email,
    @Schema(description = "Avatar URL", example = "https://www.example.com/avatar.jpg")
        String avatarUrl,
@Schema(description = "Created Timestamp",example = "2023-10-01T12:00:00Z") String gmtCreated) {
  public AssigneeVO(AssigneeDTO assigneeDTO) {
    this(
        assigneeDTO.getId().toString(),
        assigneeDTO.getAssigneeId().toString(),
        assigneeDTO.getUsername(),
        assigneeDTO.getEmail(),
        assigneeDTO.getAvatarUrl(),
        assigneeDTO.getGmtCreated().toString());
  }
}
