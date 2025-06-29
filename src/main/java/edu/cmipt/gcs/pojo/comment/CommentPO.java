package edu.cmipt.gcs.pojo.comment;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@TableName("t_activity_comment")
@NoArgsConstructor
public class CommentPO {
  private Long id;
  private Long activityId;
  private Long userId;
  private String content;
  private String codePath;
  private Integer codeLine;
  private Long parentId;
  private Timestamp gmtResolved;
  private Timestamp gmtHidden;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public CommentPO(CommentDTO comment, Long idInToken) {
    try {
      this.id = Long.valueOf(comment.id());
    } catch (NumberFormatException e) {
      this.id = null;
    }
    try {
      this.activityId = Long.valueOf(comment.activityId());
    } catch (NumberFormatException e) {
      this.activityId = null;
    }
    this.userId = idInToken;
    this.content = comment.content();
    this.codePath = comment.codePath();
    this.codeLine = comment.codeLine();
    try {
      this.parentId = Long.valueOf(comment.parentId());
    } catch (NumberFormatException e) {
      this.parentId = null;
    }
    if (comment.isResolved() != null && comment.isResolved()) {
      this.gmtResolved = new Timestamp(System.currentTimeMillis());
    } else {
      this.gmtResolved = null;
    }

    if (comment.isHidden() != null && comment.isHidden()) {
      this.gmtHidden = new Timestamp(System.currentTimeMillis());
    } else {
      this.gmtHidden = null;
    }
  }
}
