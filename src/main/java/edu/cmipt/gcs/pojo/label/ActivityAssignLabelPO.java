package edu.cmipt.gcs.pojo.label;

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
@TableName("t_activity_assign_label")
@NoArgsConstructor
public class ActivityAssignLabelPO {
  private Long id;
  private Long userId;
  private Long activityId;
  private Long labelId;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public ActivityAssignLabelPO(Long userId, Long activityId, Long labelId) {
    this(null, userId, activityId, labelId, null, null, null);
  }
}
