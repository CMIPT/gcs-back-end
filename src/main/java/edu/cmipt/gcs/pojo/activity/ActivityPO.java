package edu.cmipt.gcs.pojo.activity;

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
@TableName("t_activity")
@NoArgsConstructor
public class ActivityPO {
  private Long id;
  private Integer number;
  private Long repositoryId;
  private Long parentId;
  private String title;
  private String description;
  private Boolean isPullRequest;
  private Long userId;
  private Timestamp gmtClosed;
  private Timestamp gmtLocked;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public ActivityPO(ActivityDTO activityDTO, Integer activityNumber, String userId) {
    this(
        null,
        activityNumber,
        null,
        null,
        activityDTO.title(),
        activityDTO.description(),
        activityDTO.isPullRequest(),
        null,
        null,
        null,
        null,
        null,
        null);
    if (this.description == null) {
      this.description = "";
    }
    this.id = TypeConversionUtil.convertToLong(activityDTO.id());
    this.repositoryId = TypeConversionUtil.convertToLong(activityDTO.repositoryId());
    this.userId = TypeConversionUtil.convertToLong(userId);
    this.parentId = TypeConversionUtil.convertToLong(activityDTO.parentId());
  }

  public ActivityPO(ActivityDTO activityDTO) {
    this(activityDTO, null, null);
  }
}
