package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.GitConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.*;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.collaboration.CollaboratorVO;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.label.LabelDTO;
import edu.cmipt.gcs.pojo.label.LabelPO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.repository.CommitAuthorVO;
import edu.cmipt.gcs.pojo.repository.CommitDetailVO;
import edu.cmipt.gcs.pojo.repository.CommitVO;
import edu.cmipt.gcs.pojo.repository.DiffVO;
import edu.cmipt.gcs.pojo.repository.RepositoryDTO;
import edu.cmipt.gcs.pojo.repository.RepositoryDetailVO;
import edu.cmipt.gcs.pojo.repository.RepositoryFileVO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.repository.RepositoryVO;
import edu.cmipt.gcs.service.*;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.RedisUtil;
import edu.cmipt.gcs.util.TypeConversionUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Validated
@RestController
@Tag(name = "Repository", description = "Repository Related APIs")
public class RepositoryController {
  private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
  @Autowired private ObjectMapper objectMappter;
  @Autowired private RepositoryService repositoryService;
  @Autowired private UserService userService;
  @Autowired private UserCollaborateRepositoryService userCollaborateRepositoryService;
  @Autowired private RedisTemplate<String, Object> redisTemplate;
  @Autowired private LabelService labelService;
  @Autowired private PermissionService permissionService;

  @PostMapping(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
  @Operation(
      summary = "Create a repository",
      description = "Create a repository with the given information",
      tags = {"Repository", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void createRepository(
      @Validated(CreateGroup.class) @RequestBody RepositoryDTO repository,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long userId = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    checkRepositoryNameValidity(repository.repositoryName(), accessToken);
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
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Repository delete failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
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
          "User[{}] tried to delete repository of user[{}]", userId, repositoryPO.getUserId());
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
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Repository get failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public RepositoryDetailVO getRepositoryDetails(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "repositoryName", required = false) String repositoryName,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var repositoryPO = getRepositoryPO(id, username, repositoryName, accessToken);
    id = repositoryPO.getId();
    username = userService.getById(repositoryPO.getUserId()).getUsername();
    repositoryName = repositoryPO.getRepositoryName();
    tryUpdateUrl(repositoryPO, username);
    try (var jGitRepository = createJGitRepository(username, repositoryName)) {
      var git = new Git(jGitRepository);
      return fetchRepositoryDetails(git, repositoryPO);
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_GET_REPOSITORY_DIRECTORY_WITH_REF_API_PATH)
  @Operation(
      summary = "Get a repository's path with ref",
      description =
          "Get a repository path information with the given path and ref, path is relative to the"
              + " repository root directory, ref is the branch, tag name or hash",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public List<RepositoryFileVO> getRepositoryDirectoryWithRef(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "repositoryName", required = false) String repositoryName,
      @RequestParam("ref") String ref,
      @RequestParam("path") String path,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var repositoryPO = getRepositoryPO(id, username, repositoryName, accessToken);
    id = repositoryPO.getId();
    username = userService.getById(repositoryPO.getUserId()).getUsername();
    repositoryName = repositoryPO.getRepositoryName();
    path = normalizePath(path);
    tryUpdateUrl(repositoryPO, username);
    try (var jGitRepository = createJGitRepository(username, repositoryName)) {
      try (var git = new Git(jGitRepository)) {
        return fetchRepositoryDirectoryWithRef(git, ref, path);
      }
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_PAGE_COMMIT_WITH_REF_API_PATH)
  @Operation(
      summary = "Page commits with ref",
      description =
          "Page commits with the given ref, path is relative to the repository root directory, ref"
              + " is the branch, tag name or hash. When path is root, return the latest commits of"
              + " ref; otherwise path must not be directory and return the commits that modified"
              + " the file",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CommitVO> pageCommitWithRef(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "repositoryName", required = false) String repositoryName,
      @RequestParam("ref") String ref,
      @RequestParam("path") String path,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    var repositoryPO = getRepositoryPO(id, username, repositoryName, accessToken);
    id = repositoryPO.getId();
    username = userService.getById(repositoryPO.getUserId()).getUsername();
    repositoryName = repositoryPO.getRepositoryName();
    path = normalizePath(path);
    tryUpdateUrl(repositoryPO, username);
    try (var jGitRepository = createJGitRepository(username, repositoryName)) {
      try (var git = new Git(jGitRepository)) {
        return fetchPageCommitWithRef(git, ref, path, page, size);
      }
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_GET_REPOSITORY_COMMIT_DETAILS_API_PATH)
  @Operation(
      summary = "Get commit details",
      description = "Get commit details with the given commit hash",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public ResponseEntity<StreamingResponseBody> getCommitDetails(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "repositoryName", required = false) String repositoryName,
      @RequestParam("commitHash") String commitHash,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var repositoryPO = getRepositoryPO(id, username, repositoryName, accessToken);
    id = repositoryPO.getId();
    username = userService.getById(repositoryPO.getUserId()).getUsername();
    repositoryName = repositoryPO.getRepositoryName();
    tryUpdateUrl(repositoryPO, username);
    try (var jGitRepository = createJGitRepository(username, repositoryName)) {
      try (var git = new Git(jGitRepository)) {
        return fetchRepositoryCommitDetails(git, commitHash);
      }
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_GET_REPOSITORY_FILE_WITH_REF_API_PATH)
  @Operation(
      summary = "Get a file with given ref",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Success",
        content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public ResponseEntity<StreamingResponseBody> getFileWithRef(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "repositoryName", required = false) String repositoryName,
      @RequestParam("ref") String ref,
      @RequestParam("path") String path,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var repositoryPO = getRepositoryPO(id, username, repositoryName, accessToken);
    id = repositoryPO.getId();
    username = userService.getById(repositoryPO.getUserId()).getUsername();
    repositoryName = repositoryPO.getRepositoryName();
    path = normalizePath(path);
    tryUpdateUrl(repositoryPO, username);
    try (var jGitRepository = createJGitRepository(username, repositoryName)) {
      try (var git = new Git(jGitRepository)) {
        return fetchRepositoryFileWithRef(git, ref, path);
      }
    }
  }

  @PostMapping(ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH)
  @Operation(
      summary = "Update a repository",
      description = "Update a repository with the given information",
      tags = {"Repository", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update repository failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
  })
  public void updateRepository(
      @Validated(UpdateGroup.class) @RequestBody RepositoryDTO repository,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long id = TypeConversionUtil.convertToLong(repository.id(), true);
    var repositoryPO = repositoryService.getById(id);
    if (repositoryPO == null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id);
    }
    Long userId = repositoryPO.getUserId();
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    if (!idInToken.equals(userId)) {
      logger.info(
          "User[{}] tried to update repository of user[{}]", idInToken, repositoryPO.getUserId());
      checkVisibility(
          repositoryPO, idInToken, new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id));
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (repository.repositoryName() != null) {
      throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
    }
    if (!repositoryService.updateById(new RepositoryPO(repository))) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_UPDATE_FAILED, repository);
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH)
  @Operation(
      summary = "Check repository name validity",
      description = "Check if the repository name is valid",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        responseCode = "400",
        description = "Repository name is invalid",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void checkRepositoryNameValidity(
      @RequestParam("repositoryName")
          @Size(
              min = ValidationConstant.MIN_REPOSITORY_NAME_LENGTH,
              max = ValidationConstant.MAX_REPOSITORY_NAME_LENGTH,
              message = "{Size.repositoryController#checkRepositoryNameValidity.repositoryName}")
          @NotBlank(
              message =
                  "{NotBlank.repositoryController#checkRepositoryNameValidity.repositoryName}")
          @Pattern(
              regexp = ValidationConstant.REPOSITORY_NAME_PATTERN,
              message = "{Pattern.repositoryController#checkRepositoryNameValidity.repositoryName}")
          String repositoryName,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    if (repositoryService.getOneByUserIdAndRepositoryName(idInToken, repositoryName) != null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_ALREADY_EXISTS, repositoryName);
    }
  }

  @PostMapping(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH)
  @Operation(
      summary = "Add a collaborator",
      description = "Add a collaborator to the repository",
      tags = {"Repository", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Collaborator add failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void addCollaborator(
      @RequestParam("repositoryId") Long repositoryId,
      @RequestParam("collaborator") String collaborator,
      @RequestParam("collaboratorType") AddCollaboratorTypeEnum collaboratorType,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var userPO = collaboratorType.getOne(userService, collaborator);
    if (userPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, collaborator);
    }
    Long collaboratorId = userPO.getId();
    checkCollaborationValidity(repositoryId, collaboratorId, accessToken);
    if (!userCollaborateRepositoryService.save(
        new UserCollaborateRepositoryPO(collaboratorId, repositoryId))) {
      logger.error(
          "Failed to add collaborator[{}] to repository[{}]", collaboratorId, repositoryId);
      throw new GenericException(
          ErrorCodeEnum.COLLABORATION_ADD_FAILED, collaboratorId, repositoryId);
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_CHECK_COLLABORATION_VALIDITY_API_PATH)
  @Operation(
      summary = "Check collaboration validity",
      description = "Check if the collaboration is valid",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Collaboration is invalid",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void checkCollaborationValidity(
      @RequestParam("repositoryId") Long repositoryId,
      @RequestParam("collaborator") String collaborator,
      @RequestParam("collaboratorType") AddCollaboratorTypeEnum collaboratorType,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var userPO = collaboratorType.getOne(userService, collaborator);
    if (userPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, collaborator);
    }
    checkCollaborationValidity(repositoryId, userPO.getId(), accessToken);
  }

  @DeleteMapping(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
  @Operation(
      summary = "Remove a collaboration relationship",
      description = "Remove a collaboration with the given collaboration id",
      tags = {"Repository", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Collaboration remove failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
  })
  public void removeCollaboration(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var collaboration = userCollaborateRepositoryService.getById(id);
    if (collaboration == null) {
      throw new GenericException(ErrorCodeEnum.COLLABORATION_NOT_FOUND, id);
    }
    Long repositoryId = collaboration.getRepositoryId();
    var repositoryPO = repositoryService.getById(repositoryId);
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    if (!idInToken.equals(repositoryPO.getUserId())) {
      logger.info(
          "User[{}] tried to remove collaborator from repository[{}] whose creator is [{}]",
          idInToken,
          repositoryId,
          repositoryPO.getUserId());
      checkVisibility(
          repositoryPO, idInToken, new GenericException(ErrorCodeEnum.COLLABORATION_NOT_FOUND, id));
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (!userCollaborateRepositoryService.removeById(id)) {
      Long collaboratorId = collaboration.getCollaboratorId();
      logger.error(
          "Failed to remove collaborator[{}] from repository[{}]", collaboratorId, repositoryId);
      throw new GenericException(
          ErrorCodeEnum.COLLABORATION_REMOVE_FAILED, collaboratorId, repositoryId);
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
  @Operation(
      summary = "Page collaborators",
      description = "Page collaborators of the repository",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Collaborators page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CollaboratorVO> pageCollaborator(
      @RequestParam("repositoryId") Long repositoryId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestParam("orderBy") CollaboratorOrderByEnum orderBy,
      @RequestParam("isAsc") Boolean isAsc,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    var repository = repositoryService.getById(repositoryId);
    if (repository == null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    checkVisibility(
        repository,
        idInToken,
        new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId));
    var iPage =
        userCollaborateRepositoryService.pageCollaboratorsByRepositoryId(
            repositoryId, page, size, orderBy, isAsc);
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(CollaboratorVO::new).toList());
  }

  @GetMapping(ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH)
  @Operation(
      summary = "Page a user's repositories",
      description =
          "Page a user's repositories. If the given token is trying to get other's"
              + " repositories, only public repositories will be returned",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "User repositories page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<RepositoryVO> pageRepository(
      @RequestParam("user") String user,
      @RequestParam("userType") UserQueryTypeEnum userType,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestParam("orderBy") RepositoryOrderByEnum orderBy,
      @RequestParam("isAsc") Boolean isAsc,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    var userPO = userType.getOne(userService, user);
    if (userPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, user);
    }
    Long userId = userPO.getId();
    String idInToken = JwtUtil.getId(accessToken);
    var wrapper = new QueryWrapper<RepositoryPO>();
    if (!idInToken.equals(userId.toString())) {
      // the user only can see the public repositories of others
      wrapper.eq("is_private", false);
    }
    wrapper.eq("user_id", userId);
    wrapper.orderBy(true, isAsc, orderBy.getFieldName());
    var iPage = repositoryService.page(new Page<>(page, size), wrapper);
    return new PageVO<>(
        iPage.getTotal(),
        iPage.getRecords().stream()
            .map(
                (RepositoryPO repositoryPO) -> {
                  tryUpdateUrl(repositoryPO, userPO.getUsername());
                  return new RepositoryVO(
                      repositoryPO, userPO.getUsername(), userPO.getAvatarUrl());
                })
            .toList());
  }

  @PostMapping(ApiPathConstant.REPOSITORY_CREATE_LABEL_API_PATH)
  @Operation(
      summary = "Create a label",
      description = "Create a label in the repository",
      tags = {"Repository", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Label create failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void createLabel(
      @RequestBody LabelDTO label,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long repositoryId = TypeConversionUtil.convertToLong(label.repositoryId(), true);
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkRepositoryOperationValidity(
        repositoryId, idInToken, OperationTypeEnum.MODIFY);
    String labelName = label.name();
    String labelHexColor = label.hexColor();
    if (labelService.getOneByNameAndRepositoryId(labelName, repositoryId) != null) {
      logger.info("Label[{}] already exists in repository[{}]", labelName, repositoryId);
      throw new GenericException(ErrorCodeEnum.LABEL_NAME_ALREADY_EXISTS, labelName, repositoryId);
    }
    if (!labelService.save(new LabelPO(idInToken, label))) {
      logger.error("Failed to create label in repository[{}]", repositoryId);
      throw new GenericException(
          ErrorCodeEnum.LABEL_CREATE_FAILED, labelName, labelHexColor, repositoryId);
    }
  }

  @PostMapping(ApiPathConstant.REPOSITORY_UPDATE_LABEL_API_PATH)
  @Operation(
      summary = "Update a label",
      description = "Update a label in the repository",
      tags = {"Repository", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Label update failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateLabel(
      @RequestBody LabelDTO label,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long id = TypeConversionUtil.convertToLong(label.id(), true);
    var labelPO = labelService.getById(id);
    if (labelPO == null) {
      throw new GenericException(ErrorCodeEnum.LABEL_NOT_FOUND, id);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkRepositoryOperationValidity(
        labelPO.getRepositoryId(), idInToken, OperationTypeEnum.MODIFY);
    if (!labelService.updateById(new LabelPO(idInToken, label))) {
      logger.error("Failed to update label[{}]", id);
      throw new GenericException(ErrorCodeEnum.LABEL_UPDATE_FAILED, label);
    }
  }

  @DeleteMapping(ApiPathConstant.REPOSITORY_DELETE_LABEL_API_PATH)
  @Operation(
      summary = "Delete a label",
      description = "Delete a label in the repository",
      tags = {"Repository", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Label delete failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteLabel(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    var labelPO = labelService.getById(id);
    if (labelPO == null) {
      throw new GenericException(ErrorCodeEnum.LABEL_NOT_FOUND, id);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkRepositoryOperationValidity(
        labelPO.getRepositoryId(), idInToken, OperationTypeEnum.MODIFY);
    if (!labelService.removeById(id)) {
      logger.error("Failed to delete label[{}]", id);
      throw new GenericException(ErrorCodeEnum.LABEL_DELETE_FAILED, id);
    }
  }

  @GetMapping(ApiPathConstant.REPOSITORY_PAGE_LABEL_API_PATH)
  @Operation(
      summary = "Page label",
      description = "Page labels of the repository",
      tags = {"Repository", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Label list get failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<LabelVO> pageLabel(
      @RequestParam("repositoryId") Long repositoryId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestParam("orderBy") LabelOrderByEnum orderBy,
      @RequestParam("isAsc") Boolean isAsc,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {

    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkRepositoryOperationValidity(
        repositoryId, idInToken, OperationTypeEnum.READ);
    var wrapper = new QueryWrapper<LabelPO>();
    wrapper.eq("repository_id", repositoryId);
    wrapper.orderBy(true, isAsc, orderBy.getFieldName());
    var iPage = labelService.page(new Page<>(page, size), wrapper);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(LabelVO::new).toList());
  }

  /**
   * Create a JGit repository with the given username and repository name
   *
   * @param username the username
   * @param repositoryName the repository name
   * @return the JGit repository
   */
  private Repository createJGitRepository(String username, String repositoryName) {
    try {
      var repositoryGitPath =
          Path.of(GitConstant.GIT_SERVER_HOME, "repositories", username, repositoryName + ".git")
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

  public PageVO<CommitVO> fetchPageCommitWithRef(
      Git git, String ref, String path, Integer page, Integer size) {
    var repository = git.getRepository();
    try {
      ObjectId refId = getRefId(ref, repository);
      try (var revWalk = new RevWalk(repository)) {
        var commit = revWalk.parseCommit(refId);
        var logCommand = git.log().add(commit);
        if (path != null && !path.isBlank() && !path.equals(".")) {
          // if path is not null, only get the commits that modified the files under the given path
          var treeWalk = TreeWalk.forPath(repository, path, commit.getTree());
          if (treeWalk == null) {
            logger.info(
                "Path '{}' in ref '{}' of repo '{}' not found",
                path,
                ref,
                repository.getDirectory());
            throw new GenericException(ErrorCodeEnum.REPOSITORY_PATH_NOT_FOUND, path);
          }
          if (treeWalk.isSubtree()) {
            throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
          }
          treeWalk.close();
          logCommand.addPath(path);
        }
        var commitIter = logCommand.setMaxCount(ApplicationConstant.MAX_PAGE_TOTAL_COUNT).call();
        int skip = (page - 1) * size;
        long total = 0;
        List<CommitVO> commitList = new LinkedList<>();
        for (RevCommit commitItem : commitIter) {
          if (total >= skip && total < skip + size) {
            commitList.add(
                new CommitVO(
                    commitItem.getName(),
                    commitItem.getFullMessage(),
                    String.valueOf(commitItem.getCommitTime() * 1000L),
                    getCommitAuthorVO(commitItem)));
          }
          total++;
        }
        return new PageVO<>(total, commitList);
      }
    } catch (GenericException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fetch the repository details
   *
   * @param git the git object
   * @param repositoryPO the repository PO
   */
  private RepositoryDetailVO fetchRepositoryDetails(Git git, RepositoryPO repositoryPO) {
    try {
      String defaultRef = git.getRepository().getFullBranch();
      var userPO = userService.getById(repositoryPO.getUserId());
      String username = userPO.getUsername();
      String avatarUrl = userPO.getAvatarUrl();
      List<String> branchList = List.of();
      List<String> tagList = List.of();
      String commitHash = "", commitMessage = "", commitTimestamp = "";
      CommitAuthorVO commitAuthorVO = null;
      // not an empty repository
      if (!git.getRepository().getAllRefsByPeeledObjectId().isEmpty()) {
        branchList = git.branchList().call().stream().map(Ref::getName).toList();
        tagList = git.tagList().call().stream().map(Ref::getName).toList();
        var latestCommit =
            git.log()
                .add(getRefId(defaultRef, git.getRepository()))
                .setMaxCount(1)
                .call()
                .iterator()
                .next();
        commitAuthorVO = getCommitAuthorVO(latestCommit);
        commitHash = latestCommit.getName();
        commitMessage = latestCommit.getFullMessage();
        // convert to milliseconds
        commitTimestamp = String.valueOf(latestCommit.getCommitTime() * 1000L);
      }
      return new RepositoryDetailVO(
          repositoryPO,
          username,
          avatarUrl,
          branchList,
          tagList,
          defaultRef,
          new CommitVO(commitHash, commitMessage, commitTimestamp, commitAuthorVO));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CommitAuthorVO getCommitAuthorVO(RevCommit commit) {
    var authorIdent = commit.getAuthorIdent();
    var authorPO = userService.getOneByEmail(authorIdent.getEmailAddress());
    if (authorPO == null) {
      logger.info(
          "Author with email '{}' not found in user database, using author information from"
              + " commit",
          authorIdent.getEmailAddress());
      authorPO = userService.getOneByUsername(authorIdent.getName());
      if (authorPO == null) {
        logger.info(
            "Author with name '{}' not found in user database, using author information from"
                + " commit",
            authorIdent.getName());
      }
    }
    if (authorPO != null) {
      return new CommitAuthorVO(authorPO);
    } else {
      return new CommitAuthorVO(authorIdent);
    }
  }

  /**
   * Normalize the path, remove leading '/' and './', when the path is null or blank, default to '.'
   *
   * @param path the path
   * @return the normalized path
   */
  private String normalizePath(String path) {
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
    return path;
  }

  private ResponseEntity<StreamingResponseBody> fetchRepositoryFileWithRef(
      Git git, String ref, String path) {
    if (".".equals(path)) {
      throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
    }
    try {
      if (git.getRepository().getAllRefsByPeeledObjectId().isEmpty()) {
        throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
      }
      logger.debug("Get path content: '{}/{}'", ref, path);
      var repository = git.getRepository();
      ObjectId refId = getRefId(ref, repository);
      try (var revWalk = new RevWalk(repository)) {
        var commit = revWalk.parseCommit(refId);
        final TreeWalk treeWalk = TreeWalk.forPath(repository, path, commit.getTree());
        if (treeWalk == null) {
          logger.info(
              "Path '{}' in ref '{}' of repo '{}' not found", path, ref, repository.getDirectory());
          throw new GenericException(ErrorCodeEnum.REPOSITORY_PATH_NOT_FOUND, path);
        }
        try (treeWalk) {
          if (treeWalk.isSubtree()) {
            // Do not use this api for directory
            throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
          }
          var inputStream = repository.open(treeWalk.getObjectId(0)).openStream();
          StreamingResponseBody stream =
              outputStream -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                  outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
              };
          return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(stream);
        }
      }
    } catch (GenericException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ResponseEntity<StreamingResponseBody> fetchRepositoryCommitDetails(
      Git git, String commitHash) {
    var repository = git.getRepository();
    try {
      if (repository.getAllRefsByPeeledObjectId().isEmpty()) {
        throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
      }
      logger.debug("Get commit details: '{}'", commitHash);
      ObjectId commitId = repository.resolve(commitHash);
      if (commitId == null || !commitId.name().equals(commitHash)) {
        throw new GenericException(ErrorCodeEnum.REPOSITORY_COMMIT_NOT_FOUND, commitHash);
      }
      List<DiffVO> diffVOList = new LinkedList<>();
      RevCommit commit;
      RevCommit parentCommit = null;
      try (var revWalk = new RevWalk(repository)) {
        commit = revWalk.parseCommit(commitId);
        if (commit.getParentCount() > 0) {
          parentCommit = revWalk.parseCommit(commit.getParent(0).getId());
        }
      }
      try (var diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
        diffFormatter.setRepository(repository);
        diffFormatter.setDetectRenames(true);
        AbstractTreeIterator parentIter;
        AbstractTreeIterator commitIter;
        try (var reader = repository.newObjectReader()) {
          parentIter =
              parentCommit == null
                  ? new EmptyTreeIterator()
                  : new CanonicalTreeParser(null, reader, parentCommit.getTree());
          commitIter = new CanonicalTreeParser(null, reader, commit.getTree());
        }
        List<DiffEntry> diffEntryList = diffFormatter.scan(parentIter, commitIter);
        for (var diffEntry : diffEntryList) {
          var diffOutput = new ByteArrayOutputStream();
          try (var fileDiffFormatter = new DiffFormatter(diffOutput)) {
            fileDiffFormatter.setRepository(repository);
            fileDiffFormatter.format(diffEntry);
            fileDiffFormatter.flush();
          }
          var oldPath = diffEntry.getOldPath();
          var newPath = diffEntry.getNewPath();
          diffVOList.add(
              new DiffVO(
                  "/dev/null".equals(oldPath) ? null : oldPath,
                  "/dev/null".equals(newPath) ? null : newPath,
                  diffOutput.toString(StandardCharsets.UTF_8)));
        }
      }
      var commitDetailVO =
          new CommitDetailVO(
              commit.getName(),
              commit.getFullMessage(),
              // Convert seconds to milliseconds
              String.valueOf(commit.getCommitTime() * 1000L),
              getCommitAuthorVO(commit),
              diffVOList);
      StreamingResponseBody stream =
          outputStream -> {
            objectMappter.writeValue(outputStream, commitDetailVO);
          };
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(stream);
    } catch (GenericException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fetch the repository directory with ref
   *
   * @param git the git object
   * @param ref the ref
   * @param path the path
   * @return List of RepositoryFileVO
   */
  private List<RepositoryFileVO> fetchRepositoryDirectoryWithRef(Git git, String ref, String path) {
    try {
      // empty repository
      if (git.getRepository().getAllRefsByPeeledObjectId().isEmpty()) {
        if (!ref.isBlank()) {
          throw new GenericException(ErrorCodeEnum.REPOSITORY_REF_NOT_FOUND, ref);
        }
        if (!".".equals(path)) {
          throw new GenericException(ErrorCodeEnum.REPOSITORY_PATH_NOT_FOUND, path);
        }
        return List.of();
      }
      logger.debug("Get path content: '{}/{}'", ref, path);
      var repository = git.getRepository();
      ObjectId refId = getRefId(ref, repository);
      try (var revWalk = new RevWalk(repository)) {
        var commit = revWalk.parseCommit(refId);

        var cacheKey = RedisUtil.generateKey(this, commit.getName() + path);
        var tmp = redisTemplate.opsForValue().get(cacheKey);
        var cacheValue =
            tmp != null && tmp instanceof List<?>
                ? ((List<?>) tmp).stream().map(obj -> (RepositoryFileVO) obj).toList()
                : null;
        if (cacheValue != null) {
          logger.debug("Cache hit, key: {}, value: {}", cacheKey, cacheValue);
        } else {
          logger.debug("Cache missed, key: {}", cacheKey);
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
            if (".".equals(path)) {
              logger.debug(
                  "Path '{}' in ref '{}' of repo '{}' is the root",
                  path,
                  ref,
                  repository.getDirectory());
              treeWalk.addTree(commit.getTree());
              treeWalk.setRecursive(false);
              cacheValue = traverseDirectoryTree(treeWalk, git, refId);
            } else if (treeWalk.isSubtree()) {
              logger.debug(
                  "Path '{}' in ref '{}' of repo '{}' is a directory",
                  path,
                  ref,
                  repository.getDirectory());
              try (var dirWalk = new TreeWalk(repository)) {
                dirWalk.addTree(treeWalk.getObjectId(0));
                dirWalk.setRecursive(false);
                cacheValue = traverseDirectoryTree(dirWalk, git, refId);
              }
            } else {
              logger.debug(
                  "Path '{}' in ref '{}' of repo '{}' is a file",
                  path,
                  ref,
                  repository.getDirectory());
              // Do not use this API to get the content of a file
              throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
            }
          }
        }
        redisTemplate
            .opsForValue()
            .set(
                cacheKey,
                cacheValue,
                ApplicationConstant.SERVICE_CACHE_EXPIRATION,
                TimeUnit.MILLISECONDS);
        return cacheValue;
      }
    } catch (GenericException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ObjectId getRefId(String ref, Repository repository) throws IOException {
    try {
      ObjectId refId = repository.resolve(ref);
      return refId;
    } catch (AmbiguousObjectException | IncorrectObjectTypeException | RevisionSyntaxException e) {
      // this happens for invalid ref such as 'invalid ref'
      logger.info("Ref '{}' in repo '{}' not found", ref, repository.getDirectory());
      throw new GenericException(ErrorCodeEnum.REPOSITORY_REF_NOT_FOUND, ref);
    } catch (IOException e) {
      logger.error("Failed to resolve ref '{}' in repo '{}'", ref, repository.getDirectory());
      throw e;
    }
  }

  private List<RepositoryFileVO> traverseDirectoryTree(TreeWalk dirTree, Git git, AnyObjectId refId)
      throws Exception {
    var directory = new LinkedList<RepositoryFileVO>();
    while (dirTree.next()) {
      var name = dirTree.getNameString();
      var latestCommit = git.log().add(refId).addPath(name).setMaxCount(1).call().iterator().next();
      directory.add(
          new RepositoryFileVO(
              name,
              dirTree.isSubtree(),
              new CommitVO(
                  latestCommit.getName(),
                  latestCommit.getFullMessage(),
                  // Convert seconds to milliseconds
                  String.valueOf(latestCommit.getCommitTime() * 1000L),
                  getCommitAuthorVO(latestCommit))));
    }
    return directory;
  }

  /**
   * Get the repository PO by id or username and repository's name
   *
   * @param id the repository id
   * @param username the username
   * @param repositoryName the repository's name
   * @param accessToken the access token
   * @return the repository PO
   * @throws GenericException if the repository is not found, or the parameters are invalid
   */
  private RepositoryPO getRepositoryPO(
      Long id, String username, String repositoryName, String accessToken) {
    RepositoryPO repositoryPO;
    if (id == null) {
      if (username == null || repositoryName == null) {
        throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
      }
      var userPO = userService.getOneByUsername(username);
      if (userPO == null) {
        throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, username);
      }
      repositoryPO =
          repositoryService.getOneByUserIdAndRepositoryName(userPO.getId(), repositoryName);
    } else {
      repositoryPO = repositoryService.getById(id);
    }
    String notFoundMessage = id != null ? id.toString() : username + "/" + repositoryName;
    if (repositoryPO == null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, notFoundMessage);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    checkVisibility(
        repositoryPO,
        idInToken,
        new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, notFoundMessage));
    return repositoryPO;
  }

  /**
   * Try to update the repository's url
   *
   * @param repositoryPO the repository
   * @param username the username
   */
  private void tryUpdateUrl(RepositoryPO repositoryPO, String username) {
    // The server's domain or port may be updated, every query we try to update the url
    if (repositoryPO.generateUrl(username)) {
      repositoryService.updateById(repositoryPO);
    }
  }

  /**
   * Check the visibility of the repository
   *
   * @param repositoryPO the repository
   * @param userId the user id
   * @param notFoundMessage the message when the repository is not found
   * @throws GenericException if the repository is private and the user is not the creator or is not
   *     one of collaborators
   */
  private void checkVisibility(RepositoryPO repositoryPO, Long userId, GenericException e) {
    // If the repository is private, we return NOT_FOUND to make sure the user can't know
    // the repository exists
    if (repositoryPO.getIsPrivate()
        && !userId.equals(repositoryPO.getUserId())
        && userCollaborateRepositoryService.getOneByCollaboratorIdAndRepositoryId(
                userId, repositoryPO.getId())
            == null) {
      logger.info(
          "User[{}] tried to get private repository of user[{}]", userId, repositoryPO.getUserId());
      throw e;
    }
  }

  private void checkCollaborationValidity(
      Long repositoryId, Long collaboratorId, String accessToken) {
    var repositoryPO = repositoryService.getById(repositoryId);
    if (repositoryPO == null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    Long repositoryUserId = repositoryPO.getUserId();
    if (!idInToken.equals(repositoryUserId)) {
      logger.info(
          "User[{}] tried to add collaborator to repository[{}] whose creator is [{}]",
          idInToken,
          repositoryId,
          repositoryUserId);
      checkVisibility(
          repositoryPO,
          idInToken,
          new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId));
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (collaboratorId.equals(repositoryUserId)) {
      logger.info("User[{}] tried to add himself to repository[{}]", collaboratorId, repositoryId);
      throw new GenericException(ErrorCodeEnum.ILLOGICAL_OPERATION);
    }
    if (userCollaborateRepositoryService.getOneByCollaboratorIdAndRepositoryId(
            collaboratorId, repositoryId)
        != null) {
      logger.info(
          "Collaborator[{}] already exists in repository[{}]", collaboratorId, repositoryId);
      throw new GenericException(
          ErrorCodeEnum.COLLABORATION_ALREADY_EXISTS, collaboratorId, repositoryId);
    }
  }
}
