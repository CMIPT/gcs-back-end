package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.GitConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.AddCollaboratorTypeEnum;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.repository.RepositoryDTO;
import edu.cmipt.gcs.pojo.repository.RepositoryDetailVO;
import edu.cmipt.gcs.pojo.repository.RepositoryFileDetailVO;
import edu.cmipt.gcs.pojo.repository.RepositoryFileVO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.repository.RepositoryVO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.RepositoryService;
import edu.cmipt.gcs.service.UserCollaborateRepositoryService;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Validated
@RestController
@Tag(name = "Repository", description = "Repository Related APIs")
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    @Autowired private RepositoryService repositoryService;
    @Autowired private UserService userService;
    @Autowired private UserCollaborateRepositoryService userCollaborateRepositoryService;

    @PostMapping(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Create a repository",
            description = "Create a repository with the given information",
            tags = {"Repository", "Post Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponse(responseCode = "200", description = "Repository created successfully")
    public void createRepository(
            @Validated(CreateGroup.class) @RequestBody RepositoryDTO repository,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        Long userId = Long.valueOf(JwtUtil.getId(accessToken));
        checkRepositoryNameValidity(repository.repositoryName(), userId);
        String username = userService.getById(userId).getUsername();
        var repositoryPO = new RepositoryPO(repository, userId.toString(), username, true);
        if (!repositoryService.save(repositoryPO)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_CREATE_FAILED, repository);
        }
    }

    @DeleteMapping(ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Delete a repository",
            description = "Delete a repository with the given id",
            tags = {"Repository", "Delete Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "id",
                description = "Repository id",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Repository not found")
    })
    public void deleteRepository(
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
            @RequestParam("id") Long id) {
        var repositoryPO = repositoryService.getById(id);
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id);
        }
        String userId = JwtUtil.getId(accessToken);
        if (!userId.equals(repositoryPO.getUserId().toString())) {
            logger.info(
                    "User[{}] tried to delete repository of user[{}]",
                    userId,
                    repositoryPO.getUserId());
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (!repositoryService.removeById(id)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_DELETE_FAILED, id);
        }
    }

    @GetMapping(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
    @Operation(
            summary = "Get a repository",
            description = "Get a repository with the given id or username and repository name",
            tags = {"Repository", "Get Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "id",
                description = "Repository Id",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "username",
                description = "Username",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "repositoryName",
                description = "Repository Name",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "ref",
                description = "Ref, default to the default ref",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "path",
                description = "Path, default to '.' (root directory)",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository got successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Repository not found",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public RepositoryDetailVO getRepositoryDetails(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            @RequestParam(value = "ref", required = false) String ref,
            @RequestParam(value = "path", required = false) String path,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        RepositoryPO repositoryPO;
        if (id == null) {
            if (username == null || repositoryName == null) {
                throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
            }
            var repositoryQueryWrapper = new QueryWrapper<RepositoryPO>();
            var userQueryWrapper = new QueryWrapper<UserPO>();
            userQueryWrapper.apply("LOWER(username) = LOWER({0})", username);
            var userPO = userService.getOne(userQueryWrapper);
            if (userPO == null) {
                throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, username);
            }
            repositoryQueryWrapper.eq("user_id", userPO.getId());
            repositoryQueryWrapper.apply("LOWER(repository_name) = LOWER({0})", repositoryName);
            repositoryPO = repositoryService.getOne(repositoryQueryWrapper);
        } else {
            repositoryPO = repositoryService.getById(id);
        }
        String notFoundMessage = id != null ? id.toString() : username + "/" + repositoryName;
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, notFoundMessage);
        }
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        if (repositoryPO.getIsPrivate()
                && !idInToken.equals(repositoryPO.getUserId())
                && userCollaborateRepositoryService.getOne(
                                new QueryWrapper<UserCollaborateRepositoryPO>()
                                        .eq("collaborator_id", idInToken)
                                        .eq("repository_id", repositoryPO.getId()))
                        == null) {
            logger.info(
                    "User[{}] tried to get repository of user[{}]",
                    idInToken,
                    repositoryPO.getUserId());
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, notFoundMessage);
        }
        id = repositoryPO.getId();
        username = userService.getById(repositoryPO.getUserId()).getUsername();
        repositoryName = repositoryPO.getRepositoryName();
        // The server's domain or port may be updated, every query we try to update the url
        if (repositoryPO.generateUrl(username)) {
            repositoryService.updateById(repositoryPO);
        }
        var userPO = userService.getById(repositoryPO.getUserId());
        var repositoryVO =
                new RepositoryVO(repositoryPO, userPO.getUsername(), userPO.getAvatarUrl());
        try (var repository = createJGitRepository(username, repositoryName)) {
            var git = new Git(repository);
            return fetchRepositoryDetails(git, repositoryVO, ref, path);
        }
    }

    @PostMapping(ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Update a repository",
            description = "Update a repository with the given information",
            tags = {"Repository", "Post Method"})
    @Parameter(
            name = HeaderParameter.ACCESS_TOKEN,
            description = "Access token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Repository not found"),
        @ApiResponse(
                responseCode = "501",
                description = "Update repository name is not implemented")
    })
    public ResponseEntity<RepositoryVO> updateRepository(
            @Validated(UpdateGroup.class) @RequestBody RepositoryDTO repository,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        Long id = null;
        try {
            id = Long.valueOf(repository.id());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        var repositoryPO = repositoryService.getById(id);
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id);
        }
        Long userId = repositoryPO.getUserId();
        if (!JwtUtil.getId(accessToken).equals(userId.toString())) {
            logger.info(
                    "User[{}] tried to update repository of user[{}]",
                    userId,
                    repositoryPO.getUserId());
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (repository.repositoryName() != null) {
            throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
        }
        if (!repositoryService.updateById(new RepositoryPO(repository))) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_UPDATE_FAILED, repository);
        }
        var userPO = userService.getById(userId);
        return ResponseEntity.ok()
                .body(
                        new RepositoryVO(
                                repositoryService.getById(id),
                                userPO.getUsername(),
                                userPO.getAvatarUrl()));
    }

    @GetMapping(ApiPathConstant.REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH)
    @Operation(
            summary = "Check repository name validity",
            description = "Check if the repository name is valid",
            tags = {"Repository", "Get Method"})
    @Parameters({
        @Parameter(
                name = "userId",
                description = "User id",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "repositoryName",
                description = "Repository name",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository name is valid"),
        @ApiResponse(responseCode = "400", description = "Repository name is invalid")
    })
    public void checkRepositoryNameValidity(
            @RequestParam("repositoryName")
                    @Size(
                            min = ValidationConstant.MIN_REPOSITORY_NAME_LENGTH,
                            max = ValidationConstant.MAX_REPOSITORY_NAME_LENGTH,
                            message =
                                    "{Size.repositoryController#checkRepositoryNameValidity.repositoryName}")
                    @NotBlank(
                            message =
                                    "{NotBlank.repositoryController#checkRepositoryNameValidity.repositoryName}")
                    @Pattern(
                            regexp = ValidationConstant.REPOSITORY_NAME_PATTERN,
                            message =
                                    "{Pattern.repositoryController#checkRepositoryNameValidity.repositoryName}")
                    String repositoryName,
            @RequestParam("userId") Long userId) {
        var queryWrapper = new QueryWrapper<RepositoryPO>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.apply("LOWER(repository_name) = LOWER({0})", repositoryName);
        if (repositoryService.exists(queryWrapper)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_ALREADY_EXISTS, repositoryName);
        }
    }

    @PostMapping(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH)
    @Operation(
            summary = "Add a collaborator",
            description = "Add a collaborator to the repository",
            tags = {"Repository", "Post Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "repositoryId",
                description = "Repository ID",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "collaborator",
                description = "Collaborator's Information",
                example = "admin",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "collaboratorType",
                description = "Collaborator's Type. The value can be 'ID', 'USERNAME' or 'EMAIL'",
                example = "USERNAME",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = AddCollaboratorTypeEnum.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Collaborator added successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Collaborator or repository not found")
    })
    public void addCollaborator(
            @RequestParam("repositoryId") Long repositoryId,
            @RequestParam("collaborator") String collaborator,
            @RequestParam("collaboratorType") AddCollaboratorTypeEnum collaboratorType,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var userQueryWrapper = collaboratorType.getQueryWrapper(collaborator);
        var userPO = userService.getOne(userQueryWrapper);
        if (userPO == null) {
            throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, collaborator);
        }
        Long collaboratorId = userPO.getId();
        var repositoryPO = repositoryService.getById(repositoryId);
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
        }
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        Long repositoryUserId = repositoryPO.getUserId();
        if (!idInToken.equals(repositoryUserId)) {
            logger.info(
                    "User[{}] tried to add collaborator to repository[{}] whose creator is [{}]",
                    idInToken,
                    repositoryId,
                    repositoryUserId);
            // If the repository is private, we return NOT_FOUND to make sure the user can't know
            // the repository exists
            if (repositoryPO.getIsPrivate()
                    && userCollaborateRepositoryService.getOne(
                                    new QueryWrapper<UserCollaborateRepositoryPO>()
                                            .eq("collaborator_id", idInToken)
                                            .eq("repository_id", repositoryId))
                            == null) {
                throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
            }
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (collaboratorId.equals(repositoryUserId)) {
            logger.info(
                    "User[{}] tried to add himself to repository[{}]",
                    collaboratorId,
                    repositoryId);
            throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
        }
        var collaborationQueryWrapper = new QueryWrapper<UserCollaborateRepositoryPO>();
        collaborationQueryWrapper.eq("collaborator_id", collaboratorId);
        collaborationQueryWrapper.eq("repository_id", repositoryId);
        if (userCollaborateRepositoryService.exists(collaborationQueryWrapper)) {
            logger.info(
                    "Collaborator[{}] already exists in repository[{}]",
                    collaboratorId,
                    repositoryId);
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_ALREADY_EXISTS, collaboratorId, repositoryId);
        }
        if (!userCollaborateRepositoryService.save(
                new UserCollaborateRepositoryPO(collaboratorId, repositoryId))) {
            logger.error(
                    "Failed to add collaborator[{}] to repository[{}]",
                    collaboratorId,
                    repositoryId);
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_ADD_FAILED, collaboratorId, repositoryId);
        }
    }

    @DeleteMapping(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
    @Operation(
            summary = "Remove a collaboration relationship",
            description =
                    "Remove a collaboration relationship between a collaborator and a repository",
            tags = {"Repository", "Delete Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "repositoryId",
                description = "Repository's ID",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "collaboratorId",
                description = "Collaborator's ID",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relationship removed successfully"),
        @ApiResponse(responseCode = "404", description = "Collaboration not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
    })
    public void removeCollaboration(
            @RequestParam("repositoryId") Long repositoryId,
            @RequestParam("collaboratorId") Long collaboratorId,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var userPO = userService.getById(collaboratorId);
        if (userPO == null) {
            throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, collaboratorId);
        }
        var repositoryPO = repositoryService.getById(repositoryId);
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        // return NOT_FOUND to make sure the user can't know the repository exists
        // if the repository is private
        if (repositoryPO == null
                || repositoryPO.getIsPrivate()
                        && !idInToken.equals(repositoryPO.getUserId())
                        && userCollaborateRepositoryService.getOne(
                                        new QueryWrapper<UserCollaborateRepositoryPO>()
                                                .eq("collaborator_id", idInToken)
                                                .eq("repository_id", repositoryId))
                                == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
        }
        Long repositoryUserId = repositoryService.getById(repositoryId).getUserId();
        if (!idInToken.equals(repositoryUserId)) {
            logger.info(
                    "User[{}] tried to remove collaborator from repository[{}] whose creator is"
                            + " [{}]",
                    idInToken,
                    repositoryId,
                    repositoryUserId);
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        var queryWrapper = new QueryWrapper<UserCollaborateRepositoryPO>();
        queryWrapper.eq("collaborator_id", collaboratorId);
        queryWrapper.eq("repository_id", repositoryId);
        var userCollaborateRepositoryPO = userCollaborateRepositoryService.getOne(queryWrapper);
        if (userCollaborateRepositoryPO == null) {
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_NOT_FOUND, collaboratorId, repositoryId);
        }
        if (!userCollaborateRepositoryService.removeById(userCollaborateRepositoryPO.getId())) {
            logger.error(
                    "Failed to remove collaborator[{}] from repository[{}]",
                    collaboratorId,
                    repositoryId);
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_REMOVE_FAILED, collaboratorId, repositoryId);
        }
    }

    @GetMapping(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
    @Operation(
            summary = "Page collaborators",
            description = "Page collaborators of the repository",
            tags = {"Repository", "Get Method"})
    @Parameters({
        @Parameter(
                name = "repositoryId",
                description = "Repository ID",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "page",
                description = "Page number",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class)),
        @Parameter(
                name = "size",
                description = "Page size",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class)),
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Collaborators paged successfully"),
        @ApiResponse(responseCode = "404", description = "Repository not found")
    })
    public PageVO<UserVO> pageCollaborator(
            @RequestParam("repositoryId") Long repositoryId,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var repository = repositoryService.getById(repositoryId);
        if (repository == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
        }
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        Long userId = repository.getUserId();
        // only the creator and collaborators of the repository can page collaborators of a private
        // repository
        if (repository.getIsPrivate()
                && !idInToken.equals(userId)
                && userCollaborateRepositoryService.getOne(
                                new QueryWrapper<UserCollaborateRepositoryPO>()
                                        .eq("collaborator_id", idInToken)
                                        .eq("repository_id", repositoryId))
                        == null) {
            logger.info(
                    "User[{}] tried to page collaborators of repository[{}] whose creator is [{}]",
                    idInToken,
                    repositoryId,
                    userId);
            // We return NOT_FOUND to make sure the user can't know the repository exists
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
        }
        var iPage =
                userCollaborateRepositoryService.pageCollaboratorsByRepositoryId(
                        repositoryId, new Page<>(page, size));
        return new PageVO<>(
                iPage.getPages(),
                iPage.getTotal(),
                iPage.getRecords().stream().map(UserVO::new).toList());
    }

    @GetMapping(ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Page user repositories",
            description =
                    "Page user repositories. If the given token is trying to get other's"
                            + " repositories, only public repositories will be shown",
            tags = {"Repository", "Get Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "user",
                description = "User's Information",
                example = "admin",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "userType",
                description = "User's Type. The value can be 'ID', 'USERNAME', 'EMAIL', or 'TOKEN'",
                example = "USERNAME",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = UserQueryTypeEnum.class)),
        @Parameter(
                name = "page",
                description = "Page number",
                example = "1",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class)),
        @Parameter(
                name = "size",
                description = "Page size",
                example = "10",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class))
    })
    @ApiResponse(responseCode = "200", description = "User repositories paged successfully")
    public PageVO<RepositoryVO> pageRepository(
            @RequestParam(name = "user", required = false) String user,
            @RequestParam(name = "userType") UserQueryTypeEnum userType,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var userQueryWrapper = userType.getQueryWrapper(user, accessToken);
        var userPO = userService.getOne(userQueryWrapper);
        if (userPO == null) {
            throw new GenericException(
                    ErrorCodeEnum.USER_NOT_FOUND, user != null ? user : accessToken);
        }
        Long userId = userPO.getId();
        String idInToken = JwtUtil.getId(accessToken);
        var wrapper = new QueryWrapper<RepositoryPO>();
        if (!idInToken.equals(userId.toString())) {
            // the user only can see the public repositories of others
            wrapper.eq("is_private", false);
        }
        wrapper.eq("user_id", userId);
        var iPage = repositoryService.page(new Page<>(page, size), wrapper);
        return new PageVO<>(
                iPage.getPages(),
                iPage.getTotal(),
                iPage.getRecords().stream()
                        .map(
                                (RepositoryPO repositoryPO) -> {
                                    // The server's domain or port may be updated,
                                    // every query we try to update the url
                                    if (repositoryPO.generateUrl(userPO.getUsername())) {
                                        repositoryService.updateById(repositoryPO);
                                    }
                                    return new RepositoryVO(
                                            repositoryPO,
                                            userPO.getUsername(),
                                            userPO.getAvatarUrl());
                                })
                        .toList());
    }

    private Repository createJGitRepository(String username, String repositoryName) {
        try {
            var repositoryGitPath =
                    Path.of(
                                    GitConstant.GIT_SERVER_HOME,
                                    "repositories",
                                    username,
                                    repositoryName + ".git")
                            .toString();
            logger.debug("Repository git path: {}", repositoryGitPath);
            return new FileRepositoryBuilder()
                    .setMustExist(true)
                    .setGitDir(new File(repositoryGitPath))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create git with repository: {}/{}", username, repositoryName);
            throw new RuntimeException(e);
        }
    }

    private RepositoryDetailVO fetchRepositoryDetails(
            Git git, RepositoryVO repositoryVO, String ref, String path) {
        // remove leading '/'
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        // remove leading './'
        while (path != null && path.startsWith("./")) {
            path = path.substring(2);
        }
        // default to root directory
        if (path == null || path.isBlank()) {
            path = ".";
        }
        try {
            String defaultRef = git.getRepository().getFullBranch();
            if (ref == null || ref.isBlank()) {
                ref = defaultRef;
            }
            // empty repository
            if (git.getRepository().getAllRefsByPeeledObjectId().isEmpty()) {
                if (ref.equals(defaultRef)) {
                    if (".".equals(path)) {
                        return new RepositoryDetailVO(
                                repositoryVO,
                                List.of(),
                                List.of(),
                                "",
                                new RepositoryFileDetailVO(true, "", "", "", List.of()));
                    }
                    throw new GenericException(ErrorCodeEnum.REPOSITORY_PATH_NOT_FOUND, path);
                }
                throw new GenericException(ErrorCodeEnum.REPOSITORY_REF_NOT_FOUND, ref);
            }
            return new RepositoryDetailVO(
                    repositoryVO,
                    git.branchList().call().stream().map(Ref::getName).toList(),
                    git.tagList().call().stream().map(Ref::getName).toList(),
                    defaultRef,
                    getPathContent(git, ref, path));
        } catch (GenericException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RepositoryFileDetailVO getPathContent(Git git, String ref, String path)
            throws Exception {
        logger.debug("Get path content: '{}/{}'", ref, path);
        var repository = git.getRepository();
        ObjectId commitId = null;
        try {
            commitId = repository.resolve(ref);
        } catch (IOException e) {
            logger.error("Failed to resolve ref '{}' in repo '{}'", ref, repository.getDirectory());
            throw new RuntimeException(e);
        } catch (Exception e) {
            // this happens for invalid ref such as 'invalid ref'
            commitId = null;
        }
        if (commitId == null) {
            logger.info("Ref '{}' in repo '{}' not found", ref, repository.getDirectory());
            throw new GenericException(ErrorCodeEnum.REPOSITORY_REF_NOT_FOUND, ref);
        }
        try (var revWalk = new RevWalk(repository)) {
            var commit = revWalk.parseCommit(commitId);
            final TreeWalk treeWalk =
                    ".".equals(path)
                            ? new TreeWalk(repository)
                            : TreeWalk.forPath(repository, path, commit.getTree());
            if (treeWalk == null) {
                logger.info(
                        "Path '{}' in ref '{}' of repo '{}' not found",
                        path,
                        ref,
                        repository.getDirectory());
                throw new GenericException(ErrorCodeEnum.REPOSITORY_PATH_NOT_FOUND, path);
            }
            try (treeWalk) {
                var directory = new LinkedList<RepositoryFileVO>();
                if (".".equals(path)) {
                    logger.debug(
                            "Path '{}' in ref '{}' of repo '{}' is the root",
                            path,
                            ref,
                            repository.getDirectory());
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(false);
                    return traverseDirectoryTree(treeWalk, repository, directory);
                } else if (treeWalk.isSubtree()) {
                    logger.debug(
                            "Path '{}' in ref '{}' of repo '{}' is a directory",
                            path,
                            ref,
                            repository.getDirectory());
                    try (var dirWalk = new TreeWalk(repository)) {
                        dirWalk.addTree(treeWalk.getObjectId(0));
                        dirWalk.setRecursive(false);
                        return traverseDirectoryTree(dirWalk, repository, directory);
                    }
                } else {
                    logger.debug(
                            "Path '{}' in ref '{}' of repo '{}' is a file",
                            path,
                            ref,
                            repository.getDirectory());
                    return new RepositoryFileDetailVO(
                            false,
                            new String(repository.open(treeWalk.getObjectId(0)).getBytes()),
                            "",
                            "",
                            List.of());
                }
            }
        }
    }

    private RepositoryFileDetailVO traverseDirectoryTree(
            TreeWalk dirTree, Repository repository, List<RepositoryFileVO> directory)
            throws Exception {
        String readmeContent = "";
        String licenseContent = "";
        while (dirTree.next()) {
            directory.add(new RepositoryFileVO(dirTree.getNameString(), dirTree.isSubtree()));
            if (dirTree.isSubtree()) {
                continue;
            }
            var name = dirTree.getNameString();
            if ("README.md".equals(name)) {
                readmeContent = new String(repository.open(dirTree.getObjectId(0)).getBytes());
                logger.debug(
                        "README.md content: {}",
                        readmeContent.substring(0, Math.min(100, readmeContent.length())));
            } else if ("LICENSE".equals(name)) {
                licenseContent = new String(repository.open(dirTree.getObjectId(0)).getBytes());
                logger.debug(
                        "LICENSE content: {}",
                        licenseContent.substring(0, Math.min(100, licenseContent.length())));
            }
        }
        return new RepositoryFileDetailVO(true, "", readmeContent, licenseContent, directory);
    }
}
