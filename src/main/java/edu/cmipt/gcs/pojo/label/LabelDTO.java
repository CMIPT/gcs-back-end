package edu.cmipt.gcs.pojo.label;

import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

@Schema(description = "Label Data Transfer Object")
public record LabelDTO(
    @Schema(description = "Primary Key ID", example = "12345")
        @Null(groups = CreateGroup.class)
        @NotNull(groups = UpdateGroup.class)
        String id,
    @Schema(description = "Repository ID where the label is used", example = "repo123")
        @NotNull(groups = {CreateGroup.class, UpdateGroup.class})
        String repositoryId,
    @Schema(description = "Name of the label", example = "bug") @NotNull(groups = CreateGroup.class)
        String name,
    @Schema(description = "Hexadecimal color code for the label", example = "#FF5733")
        @NotNull(groups = CreateGroup.class)
        String hexColor,
    @Schema(description = "Description of the label", example = "Indicates a bug in the system")
        String description) {}
