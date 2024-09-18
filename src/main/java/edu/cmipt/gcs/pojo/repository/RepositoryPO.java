package edu.cmipt.gcs.pojo.repository;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
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

    public RepositoryPO(RepositoryDTO repositoryDTO, Long userId) {
        try {
            this.id = Long.valueOf(repositoryDTO.id());
        } catch (NumberFormatException e) {
            this.id = null;
        }
        this.repositoryName = repositoryDTO.repositoryName();
        this.repositoryDescription = repositoryDTO.repositoryDescription();
        this.isPrivate = repositoryDTO.isPrivate();
        this.userId = userId;
        this.star = repositoryDTO.star();
        this.fork = repositoryDTO.fork();
        this.watcher = repositoryDTO.watcher();
    }
}
