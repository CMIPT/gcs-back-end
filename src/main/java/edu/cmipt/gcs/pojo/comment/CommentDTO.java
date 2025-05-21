package edu.cmipt.gcs.pojo.comment;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Comment Data Transfer Object")
public record CommentDTO(
        @Schema(description = "Comment ID")
        @Null(groups = CreateGroup.class)
        @NotNull(groups = UpdateGroup.class)
        String id,

        @Schema(description = "Activity ID",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "12")
        @NotBlank(groups = CreateGroup.class)
        String activityId,

        @Schema(description = "The content of comment",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "gcs")
        @Size(
                groups = {CreateGroup.class, UpdateGroup.class},
                min = ValidationConstant.MIN_COMMENT_NAME_LENGTH,
                max = ValidationConstant.MAX_COMMENT_NAME_LENGTH)
        @NotBlank(groups = {CreateGroup.class,UpdateGroup.class})
        String content,

        // TODO 检验？
        @Schema(description = "Path of the code file where the comment is made. NULL if not applicable")
        String codePath,
        @Schema(description = "Line number in the code file where the comment is made. NULL if not applicable")
        Integer codeLine,

        @Schema(description = "Parent Comment ID")
        String parentId,
        @Schema(description = "Whether or Not Resolved Comment" )
        Boolean isResolved,
        @Schema(description = "Whether or Not Hide Comment")
        Boolean isHidden) {}
