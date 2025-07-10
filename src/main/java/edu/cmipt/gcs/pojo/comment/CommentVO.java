package edu.cmipt.gcs.pojo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "Comment Value Object")
public record CommentVO(
    @Schema(description = "Comment ID") String id,
    @Schema(description = "Activity ID") String activityId,
    @Schema(description = "User ID") String userId,
    @Schema(description = "The content of comment") String content,
    @Schema(description = "Name of the file where the comment is made. NULL if not applicable")
        String codePath,
    @Schema(description = "Selected line from code") Integer codeLine,
    @Schema(description = "Comment ID of this comment replies") String replyToId,
    @Schema(description = "Created timestamp, seconds since epoch") String gmtCreated,
    @Schema(description = "Updated timestamp, seconds since epoch") String gmtUpdated,
    @Schema(description = "Resolved timestamp, seconds since epoch") String gmtResolved,
    @Schema(description = "Hidden timestamp, seconds since epoch") String gmtHidden) {
  public CommentVO(CommentPO CommentPO) {
    this(
        CommentPO.getId().toString(),
        CommentPO.getActivityId().toString(),
        CommentPO.getUserId().toString(),
        CommentPO.getContent(),
        CommentPO.getCodePath(),
        CommentPO.getCodeLine(),
        String.valueOf(CommentPO.getReplyToId()),
        String.valueOf(CommentPO.getGmtCreated()),
        String.valueOf(CommentPO.getGmtUpdated()),
        String.valueOf(CommentPO.getGmtResolved()),
        String.valueOf(CommentPO.getGmtHidden()));
  }
}
