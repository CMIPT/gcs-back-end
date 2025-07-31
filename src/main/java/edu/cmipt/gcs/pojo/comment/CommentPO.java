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
  private Long creatorId;
  private String content;
  private String codePath;
  private Integer codeLine;
  private Long replyToId;
  private Long modifierId;
  private Timestamp gmtResolved;
  private Timestamp gmtHidden;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public CommentPO(CommentDTO comment, Long creatorId, Long modifierId) {
    this(
        TypeConversionUtil.convertToLong(comment.id()),
        TypeConversionUtil.convertToLong(comment.activityId()),
        creatorId,
        comment.content(),
        comment.codePath(),
        comment.codeLine(),
        TypeConversionUtil.convertToLong(comment.replyToId()),
        modifierId,
        null,
        null,
        null,
        null,
        null);
  }

  public CommentPO(CommentDTO comment, Long creatorId) {
    this(comment, creatorId, null);
  }
}
