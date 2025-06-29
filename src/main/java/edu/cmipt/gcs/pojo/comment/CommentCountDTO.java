package edu.cmipt.gcs.pojo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Comment Count Data Transfer Object")
public class CommentCountDTO {
  @Schema(description = "Comment Count")
  private Long count;

  @Schema(description = "Activity ID")
  private Long activityId;
}
