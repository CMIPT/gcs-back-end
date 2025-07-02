package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

public record ActivityDetailVO(
    @Schema(description = "id") String id,
    @Schema(description = "Activity Number") String number,
    @Schema(description = "RepositoryId") String repositoryId,
    @Schema(description = "Activity Title") String title,
    @Schema(description = "Activity Description") String description,
    @Schema(description = "Activity Labels") List<LabelVO> labels,
    @Schema(description = "Username") String username,
    @Schema(description = "Activity Assignees") List<AssigneeVO> assignees,
    @Schema(description = "The count of comment") String commentCnt,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Closed timestamp, seconds since epoch") String gmtClosed,
    @Schema(description = "Locked timestamp, seconds since epoch") String gmtLocked) {
  public ActivityDetailVO(ActivityDetailDTO activityDetailDTO) {
    this(
        activityDetailDTO.getId().toString(),
        activityDetailDTO.getNumber().toString(),
        activityDetailDTO.getRepositoryId().toString(),
        activityDetailDTO.getTitle(),
        activityDetailDTO.getDescription(),
        activityDetailDTO.getLabels(),
        activityDetailDTO.getUsername(),
        activityDetailDTO.getAssignees(),
        activityDetailDTO.getCommentCnt().toString(),
        Objects.toString(activityDetailDTO.getGmtCreated(), null),
        Objects.toString(activityDetailDTO.getGmtClosed(), null),
        Objects.toString(activityDetailDTO.getGmtLocked(), null));
  }
}
