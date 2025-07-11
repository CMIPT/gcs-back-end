package edu.cmipt.gcs.pojo.label;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

@Schema(description = "Label Data Transfer Object")
public record LabelDTO(
    @Schema(description = "Primary Key ID", example = "12345")
        @Null(groups = CreateGroup.class)
        @NotNull(groups = UpdateGroup.class)
        String id,
    @Schema(description = "Repository ID where the label is used", example = "repo123")
        @NotNull(groups = {CreateGroup.class, UpdateGroup.class})
        String repositoryId,
    @Schema(description = "Name of the label", example = "bug")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_LABEL_NAME_LENGTH,
            max = ValidationConstant.MAX_LABEL_NAME_LENGTH)
        @NotBlank(groups = CreateGroup.class)
        String name,
    @Schema(description = "Hexadecimal color code for the label", example = "#FF5733")
        @NotNull(groups = CreateGroup.class)
        String hexColor,
    @Schema(description = "Description of the label", example = "Indicates a bug in the system")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_LABEL_DESCRIPTION_LENGTH,
            max = ValidationConstant.MAX_LABEL_DESCRIPTION_LENGTH)
        String description) {}
