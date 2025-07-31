package edu.cmipt.gcs.pojo.comment;

import edu.cmipt.gcs.pojo.user.UserPO;
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
  private Long creatorId;
  private UserPO creator;
  private UserPO modifier;
  private String content;
  private String codePath;
  private Integer codeLine;
  private Long replyToId;
  private Timestamp gmtResolved;
  private Timestamp gmtHidden;
  private Timestamp gmtCreated;
  private Timestamp gmtUpdated;
  private Timestamp gmtDeleted;
}
