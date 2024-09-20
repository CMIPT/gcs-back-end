package edu.cmipt.gcs.pojo.repository;

import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Repository Data Transfer Object")
public record RepositoryDTO(
        @Schema(description = "Repository ID")
                @Null(
                        groups = CreateGroup.class,
                        message = "REPOSITORYDTO_ID_NULL {RepositoryDTO.id.Null}")
                @NotNull(
                        groups = UpdateGroup.class,
                        message = "REPOSITORYDTO_ID_NOTNULL {RepositoryDTO.id.NotNull}")
                String id,
        @Schema(
                        description = "Repository Name",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "gcs")
                @Size(
                        groups = {CreateGroup.class, UpdateGroup.class},
                        min = ValidationConstant.MIN_REPOSITORY_NAME_LENGTH,
                        max = ValidationConstant.MAX_REPOSITORY_NAME_LENGTH,
                        message =
                                "REPOSITORYDTO_REPOSITORYNAME_SIZE"
                                        + " {RepositoryDTO.repositoryName.Size}")
                @NotBlank(
                        groups = CreateGroup.class,
                        message =
                                "REPOSITORYDTO_REPOSITORYNAME_NOTBLANK"
                                        + " {RepositoryDTO.repositoryName.NotBlank}")
                @Pattern(
                        regexp = ValidationConstant.REPOSITORY_NAME_PATTERN,
                        groups = {CreateGroup.class, UpdateGroup.class},
                        message =
                                "REPOSITORYNAME_PATTERN_MISMATCH {REPOSITORYNAME_PATTERN_MISMATCH}")
                String repositoryName,
        @Schema(description = "Repository Description")
                @Size(
                        groups = {CreateGroup.class, UpdateGroup.class},
                        min = ValidationConstant.MIN_REPOSITORY_DESCRIPTION_LENGTH,
                        max = ValidationConstant.MAX_REPOSITORY_DESCRIPTION_LENGTH,
                        message =
                                "REPOSITORYDTO_REPOSITORYDESCRIPTION_SIZE"
                                        + " {RepositoryDTO.repositoryDescription.Size}")
                String repositoryDescription,
        @Schema(description = "Whether or Not Private Repo") Boolean isPrivate,
        @Schema(description = "Star Count")
                @Null(
                        groups = CreateGroup.class,
                        message = "REPOSITORYDTO_STAR_NULL {RepositoryDTO.star.Null}")
                @Min(
                        groups = UpdateGroup.class,
                        value = 0,
                        message = "REPOSITORYDTO_STAR_MIN {RepositoryDTO.star.Min}")
                Integer star,
        @Schema(description = "Fork Count")
                @Null(
                        groups = CreateGroup.class,
                        message = "REPOSITORYDTO_FORK_NULL {RepositoryDTO.fork.Null}")
                @Min(
                        groups = UpdateGroup.class,
                        value = 0,
                        message = "REPOSITORYDTO_FORK_MIN {RepositoryDTO.fork.Min}")
                Integer fork,
        @Schema(description = "Watcher Count")
                @Null(
                        groups = CreateGroup.class,
                        message = "REPOSITORYDTO_WATCHER_NULL {RepositoryDTO.watcher.Null}")
                @Min(
                        groups = UpdateGroup.class,
                        value = 0,
                        message = "REPOSITORYDTO_WATCHER_MIN {RepositoryDTO.watcher.Min}")
                Integer watcher) {}