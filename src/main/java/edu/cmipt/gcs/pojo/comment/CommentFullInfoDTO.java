package edu.cmipt.gcs.pojo.comment;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_activity_comment")
@Schema(description = "Comment Full Info Data Transfer Object")
public class CommentFullInfoDTO {
  @Schema(description = "Comment ID")
  private Long id;
  
  @Schema(description = "Activity ID")
  private Long activityId;
  
  @Schema(description = "User ID")
  private Long userId;
  
  @Schema(description = "The content of comment")
  private String content;
  
  @Schema(description = "Path of the code file where the comment is made. NULL if not applicable")
  private String codePath;
  
  @Schema(description = "Line number in the code file where the comment is made. NULL if not applicable")
  private Integer codeLine;
  
  @Schema(description = "Comment ID of this comment replies. NULL if this is a root comment")
  private Long replyToId;
  
  @Schema(description = "Resolved timestamp")
  private Timestamp gmtResolved;
  
  @Schema(description = "Hidden timestamp")
  private Timestamp gmtHidden;
  
  @Schema(description = "Created timestamp")
  private Timestamp gmtCreated;
  
  @Schema(description = "Updated timestamp")
  private Timestamp gmtUpdated;
  
  @Schema(description = "Deleted timestamp")
  private Timestamp gmtDeleted;
  
  @Schema(description = "Username of the comment author")
  private String username;
  
  @Schema(description = "Email of the comment author")
  private String email;
  
  @Schema(description = "Avatar URL of the comment author")
  private String avatarUrl;
} 