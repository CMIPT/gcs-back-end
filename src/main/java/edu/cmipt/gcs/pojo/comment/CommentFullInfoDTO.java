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
public class CommentFullInfoDTO {
  private Long id;
  private Long activityId;
  private Long userId;
  private String content;
  private String codePath;
  private Integer codeLine;
  private Long replyToId;
  private Timestamp gmtResolved;
  private Timestamp gmtHidden;
  private Timestamp gmtCreated;
  private Timestamp gmtUpdated;
  private Timestamp gmtDeleted;
  private String username;
  private String email;
  private String avatarUrl;
} 