package edu.cmipt.gcs.pojo.label;

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
@TableName("t_label")
@NoArgsConstructor
public class LabelPO {
  private Long id;
  private Long userId;
  private Long repositoryId;
  private String name;
  private String description;
  private String hexColor;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public LabelPO(Long userId, LabelDTO labelDTO) {
    this(
        null,
        userId,
        null,
        labelDTO.name(),
        labelDTO.description(),
        labelDTO.hexColor(),
        null,
        null,
        null);
    this.id = TypeConversionUtil.convertToLong(labelDTO.id());
    this.repositoryId = TypeConversionUtil.convertToLong(labelDTO.repositoryId());
  }
}
