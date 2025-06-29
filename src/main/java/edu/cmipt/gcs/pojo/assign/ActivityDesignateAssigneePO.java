package edu.cmipt.gcs.pojo.assign;

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
@TableName("t_activity_designate_assignee")
@NoArgsConstructor
public class ActivityDesignateAssigneePO {
  private Long id;
  private Long activityId;
  private Long assigneeId;
  private Long assignerId;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public ActivityDesignateAssigneePO(Long idInToken, Long activityId, Long assigneeId) {
    this.activityId = activityId;
    this.assigneeId = assigneeId;
    this.assignerId = idInToken;
  }
}
