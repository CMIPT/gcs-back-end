package edu.cmipt.gcs.pojo.repository;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.cmipt.gcs.constant.GitConstant;
import java.nio.file.Paths;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@TableName("t_repository")
@NoArgsConstructor
public class RepositoryPO {
  private Long id;
  private String repositoryName;
  private String repositoryDescription;
  private Boolean isPrivate;
  private Long userId;
  private Integer star;
  private Integer fork;
  private Integer watcher;
  private String httpsUrl;
  private String sshUrl;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public RepositoryPO(
      RepositoryDTO repositoryDTO, String userId, String username, boolean generateUrl) {
    try {
      this.id = Long.valueOf(repositoryDTO.id());
    } catch (NumberFormatException e) {
      this.id = null;
    }
    this.repositoryName = repositoryDTO.repositoryName();
    this.repositoryDescription = repositoryDTO.repositoryDescription();
    if (this.repositoryDescription == null) {
      this.repositoryDescription = "";
    }
    this.isPrivate = repositoryDTO.isPrivate();
    if (this.isPrivate == null) {
      this.isPrivate = false;
    }
    try {
      this.userId = Long.valueOf(userId);
    } catch (NumberFormatException e) {
      this.userId = null;
    }
    if (generateUrl) {
      this.generateUrl(username);
    }
  }

  public RepositoryPO(RepositoryDTO repositoryDTO, String userId, String username) {
    this(repositoryDTO, userId, username, false);
  }

  public RepositoryPO(RepositoryDTO repositoryDTO) {
    this(repositoryDTO, null, null, false);
  }

  public boolean generateUrl(String username) {
    // TODO: https is not supported now
    String httpsUrl = "";
    String sshUrl =
        new StringBuilder("ssh://")
            .append(GitConstant.GIT_SERVER_USERNAME)
            .append("@")
            .append(GitConstant.GIT_SERVER_DOMAIN)
            .append(":")
            .append(GitConstant.GIT_SERVER_PORT)
            .append(Paths.get("/", username, this.repositoryName).toString())
            .append(".git")
            .toString();
    if (!httpsUrl.equals(this.httpsUrl) || !sshUrl.equals(this.sshUrl)) {
      this.httpsUrl = httpsUrl;
      this.sshUrl = sshUrl;
      return true;
    }
    return false;
  }
}
