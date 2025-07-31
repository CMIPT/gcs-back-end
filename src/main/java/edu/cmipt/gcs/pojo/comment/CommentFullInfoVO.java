package edu.cmipt.gcs.pojo.comment;

import edu.cmipt.gcs.pojo.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

@Schema(description = "Comment Full Info Value Object")
public record CommentFullInfoVO(
    @Schema(description = "Comment ID") String id,
    @Schema(description = "Activity ID") String activityId,
    @Schema(description = "Creator Info") UserVO creator,
    @Schema(description = "Modifier Info") UserVO modifier,
    @Schema(description = "The content of comment") String content,
    @Schema(description = "Name of the file where the comment is made. NULL if not applicable") String codePath,
    @Schema(description = "Selected line from code") Integer codeLine,
    @Schema(description = "Comment ID of this comment replies") String replyToId,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Updated timestamp, seconds since epoch") String gmtUpdated,
    @Schema(description = "Resolved timestamp, seconds since epoch") String gmtResolved,
    @Schema(description = "Hidden timestamp, seconds since epoch") String gmtHidden) {
  
  public CommentFullInfoVO(CommentFullInfoDTO commentFullInfoDTO) {
    this(
        commentFullInfoDTO.getId().toString(),
        commentFullInfoDTO.getActivityId().toString(),
        Optional.ofNullable(commentFullInfoDTO.getCreator()).map(UserVO::new).orElse(null),  // 使用 Optional
        Optional.ofNullable(commentFullInfoDTO.getModifier()).map(UserVO::new).orElse(null),   // 使用 Optional
        commentFullInfoDTO.getContent(),
        commentFullInfoDTO.getCodePath(),
        commentFullInfoDTO.getCodeLine(),
        String.valueOf(commentFullInfoDTO.getReplyToId()),
        String.valueOf(commentFullInfoDTO.getGmtCreated()),
        String.valueOf(commentFullInfoDTO.getGmtUpdated()),
        String.valueOf(commentFullInfoDTO.getGmtResolved()),
        String.valueOf(commentFullInfoDTO.getGmtHidden()));
  }
} 