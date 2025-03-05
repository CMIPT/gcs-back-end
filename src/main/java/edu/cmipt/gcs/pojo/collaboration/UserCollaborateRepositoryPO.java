package edu.cmipt.gcs.pojo.collaboration;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
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
  private LocalDateTime gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime gmtUpdated;

  @TableLogic private LocalDateTime gmtDeleted;

  public UserCollaborateRepositoryPO(Long collaboratorId, Long repositoryId) {
    this.collaboratorId = collaboratorId;
    this.repositoryId = repositoryId;
  }
}
