package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.enumeration.ActivityOrderByEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.validation.group.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Activity Page Query Data Transfer Object")
public record ActivityQueryDTO(
        @Schema(description = "User Information", example = "Name/ID/Token")
        @NotNull(groups = QueryGroup.class)
        String user,

        @Schema(description = "Type of User", example = "name")
        @NotNull(groups = QueryGroup.class)
        UserQueryTypeEnum userType,

        @Schema(description = "Repository ID", example = "11")
        @NotNull(groups = QueryGroup.class)
        String repositoryId,

        @Schema(description = "Author of the Activity", example = "author123")
        String author,

        @Schema(description = "is the Activity a Pull Request?", example = "true")
        @NotNull(groups = QueryGroup.class)
        Boolean isPullRequest,

        @Schema(description = "Labels associated with the Activity", example = "[\"bug\", \"enhancement\"]")
        List<String> labels,

        @Schema(description = "Assignees of the Activity", example = "[\"assignee1\", \"assignee2\"]")
        List<String> assignees,

        @Schema(description = "Order By Field", example = "CREATED_AT")
        ActivityOrderByEnum orderBy,

        @Schema(description = "Sort Order (true for ascending, false for descending)", example = "true")
        Boolean isAsc,

        @Schema(description = "Whether or not the activity is locked", example = "false")
        Boolean isLocked,

        @Schema(description = "Whether or not the activity is closed", example = "false")
        Boolean isClosed
)
{}