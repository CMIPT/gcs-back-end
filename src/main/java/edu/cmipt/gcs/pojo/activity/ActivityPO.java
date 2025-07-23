package edu.cmipt.gcs.pojo.activity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import edu.cmipt.gcs.util.TypeConversionUtil;
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
  private Long creatorId;
  private Long modifierId;
  private Timestamp gmtClosed;
  private Timestamp gmtLocked;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public ActivityPO(ActivityDTO activityDTO, Integer activityNumber, Long creatorId, Long modifierId) {
    this(
        TypeConversionUtil.convertToLong(activityDTO.id()),
        activityNumber,
        TypeConversionUtil.convertToLong(activityDTO.repositoryId()),
        TypeConversionUtil.convertToLong(activityDTO.parentId()),
        activityDTO.title(),
        activityDTO.description(),
        activityDTO.isPullRequest(),
        creatorId,
        modifierId,
        null,
        null,
        null,
        null,
        null);
  }

  public ActivityPO(ActivityDTO activityDTO,Integer activityNumber, Long creatorId) {
    this(activityDTO, activityNumber, creatorId, null);
  }

  public ActivityPO(ActivityDTO activityDTO, Long modifierId) {
      this(activityDTO, null, null, modifierId);
  }
}
