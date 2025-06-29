package edu.cmipt.gcs.pojo.label;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Activity Assign Label Value Object")
public record ActivityAssignLabelVO(
    String id,
    String userId,
    String labelId,
    String labelName,
    String labelDescription,
    String labelHexColor) {
  public ActivityAssignLabelVO(ActivityAssignLabelDTO activityAssignLabelDTO) {
    this(
        activityAssignLabelDTO.getId().toString(),
        activityAssignLabelDTO.getUserId().toString(),
        activityAssignLabelDTO.getLabelId().toString(),
        activityAssignLabelDTO.getLabelName(),
        activityAssignLabelDTO.getLabelDescription(),
        activityAssignLabelDTO.getLabelHexColor());
  }
}
