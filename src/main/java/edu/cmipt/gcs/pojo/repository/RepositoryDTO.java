package edu.cmipt.gcs.pojo.repository;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Repository Data Transfer Object")
public record RepositoryDTO(
    @Schema(description = "Repository ID")
        @Null(groups = CreateGroup.class)
        @NotNull(groups = UpdateGroup.class)
        String id,
    @Schema(
            description = "Repository Name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "gcs")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_REPOSITORY_NAME_LENGTH,
            max = ValidationConstant.MAX_REPOSITORY_NAME_LENGTH)
        @NotBlank(groups = CreateGroup.class)
        @Pattern(
            regexp = ValidationConstant.REPOSITORY_NAME_PATTERN,
            groups = {CreateGroup.class, UpdateGroup.class})
        String repositoryName,
    @Schema(description = "Repository Description")
        @Size(
            groups = {CreateGroup.class, UpdateGroup.class},
            min = ValidationConstant.MIN_REPOSITORY_DESCRIPTION_LENGTH,
            max = ValidationConstant.MAX_REPOSITORY_DESCRIPTION_LENGTH)
        String repositoryDescription,
    @Schema(description = "Whether or Not Private Repo", example = "false") Boolean isPrivate) {}
