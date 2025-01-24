package edu.cmipt.gcs.pojo.repository;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import edu.cmipt.gcs.constant.GitConstant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Paths;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("t_repository")
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
    private LocalDateTime gmtCreated;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtUpdated;

    @TableLogic private LocalDateTime gmtDeleted;

    public RepositoryPO(RepositoryDTO repositoryDTO, String userId, boolean generateUrl) {
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
            // TODO: https is not supported now
            this.httpsUrl = "";
            this.sshUrl =
                    new StringBuilder("ssh://")
                            .append(GitConstant.GIT_SERVER_USERNAME)
                            .append("@")
                            .append(GitConstant.GIT_SERVER_DOMAIN)
                            .append(":")
                            .append(GitConstant.GIT_SERVER_PORT)
                            .append(Paths.get("/", userId.toString(), repositoryName).toString())
                            .toString();
        }
    }

    public RepositoryPO(RepositoryDTO repositoryDTO, String userId) {
        this(repositoryDTO, userId, false);
    }

    public RepositoryPO(RepositoryDTO repositoryDTO) {
        this(repositoryDTO, null, false);
    }
}
