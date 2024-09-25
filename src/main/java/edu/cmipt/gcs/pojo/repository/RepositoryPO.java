package edu.cmipt.gcs.pojo.repository;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;

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
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtUpdated;
    @TableLogic private LocalDateTime gmtDeleted;

    public RepositoryPO(RepositoryDTO repositoryDTO, String userId) {
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
        try {
            this.userId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            this.userId = null;
        }
        this.star = repositoryDTO.star();
        this.fork = repositoryDTO.fork();
        this.watcher = repositoryDTO.watcher();
    }

    public RepositoryPO(RepositoryDTO repositoryDTO) {
        this(repositoryDTO, null);
    }
}
