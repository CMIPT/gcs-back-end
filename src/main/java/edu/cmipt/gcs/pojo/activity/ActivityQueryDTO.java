package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.enumeration.ActivityOrderByEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.validation.group.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Activity Page Query Data Transfer Object")
public record ActivityQueryDTO(
    @Schema(
            description = "User Information",
            example =
                "Name:author123/ID:1935328433259216897/"
                    + "Token:eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NTE0NTQzNzAsImlkIjoxOTM3MTE1ODc5Mjc3ODk5Nzc4LCJ0b2tlblR5cGUiOiJBQ0NFU1NfVE9LRU4ifQ.sEdCjIL--0IlEFlnM5qw_p4HLozCfxnth7EzOBNpbwo")
        @NotNull(groups = QueryGroup.class)
        String user,
    @Schema(description = "Type of User", example = "Name") @NotNull(groups = QueryGroup.class)
        UserQueryTypeEnum userType,
    @Schema(description = "Repository ID", example = "1936351319541448705")
        @NotNull(groups = QueryGroup.class)
        String repositoryId,
    @Schema(description = "The name of the Activity Author", example = "author123") String author,
    @Schema(description = "Whether or not the Activity is a Pull Request", example = "true")
        @NotNull(groups = QueryGroup.class)
        Boolean isPullRequest,
    @Schema(
            description = "Labels' names associated with the Activity",
            example = "[\"bug\", \"enhancement\"]")
        List<String> labels,
    @Schema(
            description = "Assignees' names of the Activity",
            example = "[\"assignee1\", \"assignee2\"]")
        List<String> assignees,
    @Schema(description = "Order By Field", example = "CREATED_AT") ActivityOrderByEnum orderBy,
    @Schema(description = "Sort Order (true for ascending, false for descending)", example = "true")
        Boolean isAsc,
    @Schema(description = "Whether or not the activity is locked", example = "false")
        Boolean isLocked,
    @Schema(description = "Whether or not the activity is closed", example = "false")
        Boolean isClosed) {}
