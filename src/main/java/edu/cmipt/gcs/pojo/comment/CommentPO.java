package edu.cmipt.gcs.pojo.comment;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.cmipt.gcs.util.TypeConversionUtil;
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
    this(
        null,
        null,
        idInToken,
        comment.content(),
        comment.codePath(),
        comment.codeLine(),
        null,
        null,
        null,
        null,
        null,
        null);
    this.id = TypeConversionUtil.convertToLong(comment.id());
    this.activityId = TypeConversionUtil.convertToLong(comment.activityId());
    this.parentId = TypeConversionUtil.convertToLong(comment.parentId());
    this.gmtResolved = getResolvedTimeSinceEpoch(comment.isResolved());
    this.gmtHidden = getHiddenTimeSinceEpoch(comment.isHidden());
  }

  private Timestamp getHiddenTimeSinceEpoch(Boolean isHidden) {
    if (isHidden != null && isHidden) {
      return this.gmtHidden != null ? this.gmtHidden : new Timestamp(System.currentTimeMillis());
    } else {
      return null;
    }
  }

  private Timestamp getResolvedTimeSinceEpoch(Boolean isResolved) {
    if (isResolved != null && isResolved) {
      return this.gmtResolved != null
          ? this.gmtResolved
          : new Timestamp(System.currentTimeMillis());
    } else {
      return null;
    }
  }
}
