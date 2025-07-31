package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import edu.cmipt.gcs.pojo.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;

public record ActivityFullInfoVO(
    @Schema(description = "id") String id,
    @Schema(description = "Activity Number") String number,
    @Schema(description = "Repository Id") String repositoryId,
    @Schema(description = "Repository Name") String repositoryName,
    @Schema(description = "Activity Title") String title,
    @Schema(description = "Activity Description") String description,
    @Schema(description = "Activity Labels") List<LabelVO> labels,
    @Schema(description = "Creator Info") UserVO creator,
    @Schema(description = "Modifier Info") UserVO modifier,
    @Schema(description = "Activity Assignees") List<AssigneeVO> assignees,
    @Schema(description = "The count of comment") String commentCnt,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Updated timestamp, seconds since epoch") String gmtUpdated,
    @Schema(description = "Closed timestamp, seconds since epoch") String gmtClosed,
    @Schema(description = "Locked timestamp, seconds since epoch") String gmtLocked) {
  public ActivityFullInfoVO(ActivityFullInfoDTO activityDetailDTO) {
    this(
        activityDetailDTO.getId().toString(),
        activityDetailDTO.getNumber().toString(),
        activityDetailDTO.getRepositoryId().toString(),
        activityDetailDTO.getRepositoryName(),
        activityDetailDTO.getTitle(),
        activityDetailDTO.getDescription(),
        activityDetailDTO.getLabels().stream().map(LabelVO::new).toList(),
        Optional.ofNullable(activityDetailDTO.getCreator())
            .map(UserVO::new)
            .orElse(null), // 使用 Optional
        Optional.ofNullable(activityDetailDTO.getModifier())
            .map(UserVO::new)
            .orElse(null), // 使用 Optional
        activityDetailDTO.getAssignees().stream().map(AssigneeVO::new).toList(),
        activityDetailDTO.getCommentCnt().toString(),
        String.valueOf(activityDetailDTO.getGmtCreated()),
        String.valueOf(activityDetailDTO.getGmtUpdated()),
        String.valueOf(activityDetailDTO.getGmtClosed()),
        String.valueOf(activityDetailDTO.getGmtLocked()));
  }
}
