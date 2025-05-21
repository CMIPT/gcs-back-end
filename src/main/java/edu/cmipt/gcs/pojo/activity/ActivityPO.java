package edu.cmipt.gcs.pojo.activity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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

    @TableLogic
    private Timestamp gmtDeleted;

    public ActivityPO(ActivityDTO activityDTO, Integer activityNumber, String userId) {
        try {
            this.id = Long.valueOf(activityDTO.id());
        } catch (NumberFormatException e) {
            this.id = null;
        }
        try {
            this.repositoryId = Long.valueOf(activityDTO.repositoryId());
        } catch (NumberFormatException e) {
            this.repositoryId = null;
        }
        try {
            this.userId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            this.userId = null;
        }
        this.number = activityNumber;
        this.isPullRequest = false;
        this.title = activityDTO.title();
        this.description = activityDTO.description();
        this.gmtClosed = activityDTO.gmtClosed();
        this.gmtLocked = activityDTO.gmtLocked();

    }
    public ActivityPO(ActivityDTO activityDTO) {
        this(activityDTO, null, null);
    }

}
