package edu.cmipt.gcs.pojo.collaboration;

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
@TableName("t_user_collaborate_repository")
@NoArgsConstructor
public class UserCollaborateRepositoryPO {
  private Long id;
  private Long collaboratorId;
  private Long repositoryId;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public UserCollaborateRepositoryPO(Long collaboratorId, Long repositoryId) {
    this(null, collaboratorId, repositoryId, null, null, null);
  }
}
