package edu.cmipt.gcs.pojo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User Activity Assignee Data Transfer Object")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDesignateAssigneeDTO {
    @Schema(description = "Assignment ID")
    private Long id;

    @Schema(description = "Activity ID")
    private Long activityId;

    @Schema(description = "Assignee ID")
    private Long assigneeId;

    @Schema(description = "User Name")
    private String username;

    @Schema(description = "User Avatar URL")
    private String avatarUrl;

    @Schema(description = "User Email")
    private String email;

}
