package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;

@Schema(description = "Activity Data Transfer Object")
public record ActivityDTO(
    @Schema(description = "Primary Key ID")
        @Null(groups = CreateGroup.class)
        @NotNull(groups = UpdateGroup.class)
        String id,
    @Schema(description = "Activity Number") @Null(groups = CreateGroup.class) String number,
    @Schema(description = "Repository ID") @NotNull(groups = CreateGroup.class) String repositoryId,
    @Schema(description = "Parent Activity ID, NULL if this is a root activity") String parentId,
    @Schema(description = "Activity Description")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_ACTIVITY_DESCRIPTION_LENGTH,
            max = ValidationConstant.MAX_ACTIVITY_DESCRIPTION_LENGTH)
        String description,
    @Schema(
            description = "Activity Title",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "An example activity title")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_ACTIVITY_TITLE_LENGTH,
            max = ValidationConstant.MAX_ACTIVITY_TITLE_LENGTH)
        @NotBlank(groups = CreateGroup.class)
        String title,
    @Schema(description = "if the activity is a pull request,true: pull request, false: issue")
        @NotNull(groups = {CreateGroup.class, UpdateGroup.class})
        Boolean isPullRequest,
    @Schema(description = "whether or not the activity is locked", example = "false")
        Boolean isClosed,
    @Schema(description = "whether or not the activity is closed", example = "false")
        Boolean isLocked) {}
