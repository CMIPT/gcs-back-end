package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.*;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.activity.*;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneePO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.comment.CommentDTO;
import edu.cmipt.gcs.pojo.comment.CommentFullInfoDTO;
import edu.cmipt.gcs.pojo.comment.CommentFullInfoVO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.issue.IssueVO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.service.*;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.TypeConversionUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.QueryGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@Tag(name = "Activity", description = "Activity Related API")
public class ActivityController {
  private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

  @Autowired private UserService userService;
  @Autowired private ActivityService activityService;
  @Autowired private CommentService commentService;
  @Autowired private ActivityAssignLabelService activityAssignLabelService;
  @Autowired private ActivityDesignateAssigneeService activityDesignateAssigneeService;
  @Autowired private LabelService labelService;
  @Autowired private PermissionService permissionService;
  @Autowired private RepositoryService repositoryService;

  @PostMapping(ApiPathConstant.ACTIVITY_CREATE_ACTIVITY_API_PATH)
  @Operation(
      summary = "Create an activity",
      description = "Create an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Failure",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void createActivity(
      @Validated(CreateGroup.class) @RequestBody ActivityDTO activity,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken)
      throws InterruptedException {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    Long repositoryId = TypeConversionUtil.convertToLong(activity.repositoryId(), true);
    permissionService.checkRepositoryOperationValidity(
        repositoryId, idInToken, OperationTypeEnum.ATTACH_ACTIVITY);
    // 检查是否涉及sub-issue
    if (activity.parentId() != null) {
      // 只有issue可以有父活动
      if (activity.isPullRequest()) {
        throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
      }
      Long parentId = TypeConversionUtil.convertToLong(activity.parentId(), true);
      checkSubIssueCreationValidity(repositoryId, parentId);
    }
    int retry = 0;
    while (retry < ApplicationConstant.CREATE_ACTIVITY_MAX_RETRY_TIMES) {
      try {
        ActivityPO latestActivityPO = activityService.getLatestActivityByRepositoryId(repositoryId);
        int number = (latestActivityPO == null ? 1 : latestActivityPO.getNumber() + 1);
        var activityPO = new ActivityPO(activity, number, idInToken.toString());
        activityService.save(activityPO);
        return;
      } catch (DuplicateKeyException e) {
        retry++;
        // 短暂 sleep 重试
        Thread.sleep(50);
      }
    }
    throw new GenericException(ErrorCodeEnum.ACTIVITY_CREATE_FAILED, activity);
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_DELETE_ACTIVITY_API_PATH)
  @Operation(
      summary = "Delete an activity",
      description = "Delete an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Activity delete failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteActivity(
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
      @RequestParam("id") Long id) {
    // do not support delete activity by now
    throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
    //    var activityPO = activityService.getById(id);
    //    if (activityPO == null) {
    //      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, id);
    //    }
    //    String userId = JwtUtil.getId(accessToken);
    //    // only admin can delete activity
    //    if (!userId.equals(activityPO.getUserId().toString())) {
    //      logger.info("User[{}] tried to delete activity of user[{}]", userId,
    // activityPO.getUserId());
    //      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    //    }
    //    if (!activityService.removeById(id)) {
    //      throw new GenericException(ErrorCodeEnum.ACTIVITY_DELETE_FAILED, id);
    //    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_LOCKED_STATE_API_PATH)
  @Operation(
      summary = "Update an activity locked state",
      description = "Update an activity locked state with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update activity locked state failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateActivityLockedState(
      @RequestParam("id") Long id,
      @RequestParam("isLocked") Boolean isLocked,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(id, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    if (!activityService.updateLockedState(id, isLocked)) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CLOSED_STATE_API_PATH)
  @Operation(
      summary = "Update an activity closed state",
      description = "Update an activity closed state with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update activity closed state failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateActivityClosedState(
      @RequestParam("id") Long id,
      @RequestParam("isClosed") Boolean isClosed,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(id, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    if (!activityService.updateClosedState(id, isClosed)) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH)
  @Operation(
      summary = "Update an activity content",
      description = "Update an activity content with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update activity content failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateActivityContent(
      @Validated(UpdateGroup.class) @RequestBody ActivityDTO activity,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {

    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    Long activityId = TypeConversionUtil.convertToLong(activity.id(), true);
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    // 检查是否涉及sub-issue
    if (activity.parentId() != null) {
      Long parentId = TypeConversionUtil.convertToLong(activity.parentId(), true);
      var activityPO = activityService.getById(activityId);
      // 不能更新父活动为自己 并且 只有issue可以有父活动
      if (parentId.equals(activityId) || activityPO.getIsPullRequest()) {
        throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
      }
      checkSubIssueCreationValidity(activityPO.getRepositoryId(), parentId);
    }
    if (!activityService.updateById(new ActivityPO(activity))) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, activityId);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH)
  @Operation(
      summary = "Page a repository's activities full information",
      description =
          "Page a repository's activities full information. If the given token is trying to get"
              + " other repository's activities, only public repository's activities will be"
              + " returned",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "repository activities page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<ActivityFullInfoVO> pageActivityFullInfo(
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @Validated(QueryGroup.class) @RequestBody ActivityQueryDTO activityQueryDTO,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    Long repositoryId = TypeConversionUtil.convertToLong(activityQueryDTO.repositoryId(), true);
    permissionService.checkRepositoryOperationValidity(
        repositoryId,
        TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true),
        OperationTypeEnum.READ);
    var iPage = activityService.pageActivityFullInfo(activityQueryDTO, page, size);
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(ActivityFullInfoVO::new).toList());
  }

  @PostMapping(ApiPathConstant.ACTIVITY_CREATE_SUB_ISSUE_API_PATH)
  @Operation(
      summary = "Create a sub-issue to an issue",
      description = "Create a sub-issue to an issue with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Create sub-issue failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void createSubIssueToIssue(
      @Validated(CreateGroup.class) @RequestBody ActivityDTO activity,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken)
      throws InterruptedException {
    // do not support issue module now
    throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
//    if (activity.isPullRequest() || activity.parentId() == null) {
//      throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
//    }
//    createActivity(activity, accessToken);
  }

  @PostMapping(ApiPathConstant.ACTIVITY_ADD_SUB_ISSUE_API_PATH)
  @Operation(
      summary = "Add a sub-issue to an issue",
      description = "Add a sub-issue to an issue with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Add sub-issue failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void addSubIssueToIssue(
      @RequestParam("parentId") Long parentId,
      @RequestParam("subIssueId") Long subIssueId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    // do not support issue module now
    throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
//    ActivityDTO activity = new ActivityDTO(subIssueId, parentId);
//    updateActivityContent(activity, accessToken);
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_REMOVE_SUB_ISSUE_API_PATH)
  @Operation(
      summary = "Remove a sub-issue from an activity",
      description = "Remove a sub-issue from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Delete sub-issue failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void removeSubIssue(
      @RequestParam("subIssueId") Long subIssueId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    // do not support issue module now
    throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
//    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
//    permissionService.checkActivityOperationValidity(
//        subIssueId, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
//    var activityPO = activityService.getById(subIssueId);
//    if (activityPO.getIsPullRequest()) {
//      throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
//    }
//    if (!activityService.removeSubIssueById(subIssueId)) {
//      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, subIssueId);
//    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_SUB_ISSUE_API_PATH)
  @Operation(
      summary = "Page sub-issues of an issue",
      description =
          "Page sub-issues of an issue. If the given token is trying to get other activity's"
              + " sub-issues, only public activity's sub-issues will be returned",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "activity sub-issues page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<IssueVO> pageSubIssueOfIssue(
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestParam("parentId") Long parentId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    // do not support issue module now
    throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
//    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
//      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
//    }
//    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
//    permissionService.checkActivityOperationValidity(parentId, idInToken, OperationTypeEnum.READ);
//    var activityPO = activityService.getById(parentId);
//    if (activityPO.getIsPullRequest()) {
//      throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
//    }
//    var iPage = activityService.pageSubIssueByParentId(parentId, page, size);
//    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(IssueVO::new).toList());
  }

  @GetMapping(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
  @Operation(
      summary = "Get an activity full information",
      description = "Get an activity full information by id or activity number and repository id",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Activity not found",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
    @ApiResponse(
        description = "Message conversion error",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public ActivityFullInfoVO getActivityFullInfo(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "activityNumber", required = false) Long activityNumber,
      @RequestParam(value = "repositoryId", required = false) Long repositoryId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    ActivityPO activityPO;
    if (id == null) {
      if (activityNumber == null || repositoryId == null)
        throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
      activityPO =
          activityService.getOneByActivityNumberAndRepositoryId(activityNumber, repositoryId);
    } else {
      activityPO = activityService.getById(id);
    }
    String notFoundMessage = id != null ? id.toString() : repositoryId + "/" + activityNumber;
    if (activityPO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage);
    }
    id = activityPO.getId();
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(id, idInToken, OperationTypeEnum.READ);
    ActivityFullInfoDTO activityFullInfoDTO = activityService.getActivityFullInfoById(id);
    if (activityFullInfoDTO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage);
    }
    return new ActivityFullInfoVO(activityFullInfoDTO);
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_CONTENT_API_PATH)
  @Operation(
      summary = "Update a comment content of an activity",
      description = "Update a comment content of an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update comment content failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateCommentContentOfActivity(
      @Validated(UpdateGroup.class) @RequestBody CommentDTO comment,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    Long commentId = TypeConversionUtil.convertToLong(comment.id(), true);
    permissionService.checkCommentOperationValidity(
            commentId, idInToken, OperationTypeEnum.MODIFY_COMMENT);
    if (!commentService.updateById(new CommentPO(comment, idInToken))) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, comment);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
  @Operation(
      summary = "Update a comment hidden state of an activity",
      description = "Update a comment hidden state of an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update comment hidden state failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateCommentHiddenStateOfActivity(
      @RequestParam("id") Long id,
      @RequestParam("isHidden") Boolean isHidden,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkCommentOperationValidity(
        id, idInToken, OperationTypeEnum.MODIFY_COMMENT);
    if (!commentService.updateHiddenState(id, isHidden)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
  @Operation(
      summary = "Update a comment resolved state of an activity",
      description = "Update a comment resolved state of an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update comment resolved state failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateCommentResolvedStateOfActivity(
      @RequestParam("id") Long id,
      @RequestParam("isResolved") Boolean isResolved,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkCommentOperationValidity(
        id, idInToken, OperationTypeEnum.MODIFY_COMMENT);
    if (!commentService.updateResolvedState(id, isResolved)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, id);
    }
  }

  // TODO: 在涉及回复评论时(pr的code view)，如果删除的是根评论，要更新新的根评论和reply_to_id
  @DeleteMapping(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
  @Operation(
      summary = "Delete a comment from an activity",
      description = "Delete a comment from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Comment delete failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteCommentFromActivity(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkCommentOperationValidity(
        id, idInToken, OperationTypeEnum.MODIFY_COMMENT);
    if (!commentService.removeById(id)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_DELETE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
  @Operation(
      summary = "Create a comment to an activity",
      description = "Create a comment to an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Create comment failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
  })
  public void createCommentToActivity(
      @Validated(CreateGroup.class) @RequestBody CommentDTO comment,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    Long activityId = TypeConversionUtil.convertToLong(comment.activityId(), true);
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.ATTACH_COMMENT);
    if (comment.replyToId() != null) {
      Long parentId = TypeConversionUtil.convertToLong(comment.replyToId(), true);
      var parentCommentPO = commentService.getById(parentId);
      // 检查评论的父评论是否存在 并且父评论的活动ID和当前评论的活动ID必须一致
      if (parentCommentPO == null || !activityId.equals(parentCommentPO.getActivityId())) {
        throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, parentId);
      }
    }
    if (!commentService.save(new CommentPO(comment, idInToken))) {
      throw new GenericException(ErrorCodeEnum.COMMENT_CREATE_FAILED, comment);
    }
  }

  // TODO: 在PR的code view中，评论的分页需要包含子评论
  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_COMMENT_API_PATH)
  @Operation(
      summary = "Page comments of an activity",
      description = "Page comments of an activity",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Page comments failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CommentFullInfoVO> pageCommentOfActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    Page<CommentFullInfoDTO> iPage = commentService.pageCommentFullInfoByActivityId(page, size, activityId);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(CommentFullInfoVO::new).toList());
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_SUB_COMMENT_API_PATH)
  @Operation(
      summary = "Page sub-comments of an activity comment",
      description = "Page sub-comments of an activity comment",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Page sub-comments failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CommentFullInfoVO> pageSubCommentOfActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("replyToId") Long replyToId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    Page<CommentFullInfoDTO> iPage =
        commentService.pageSubCommentFullInfoByReplyToId(page, size, replyToId);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(CommentFullInfoVO::new).toList());
  }

  @PostMapping(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
  @Operation(
      summary = "Add a label to an activity",
      description = "Add a label to an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Add label to activity failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void addLabelToActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("labelId") Long labelId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    // 检查这个标签是否存在于当前活动对应的仓库中
    var activityPO = activityService.getById(activityId);
    if (activityPO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId);
    }
    Long repositoryId = activityPO.getRepositoryId();
    var LabelPO = labelService.getById(labelId);
    if (LabelPO == null || !repositoryId.equals(LabelPO.getRepositoryId())) {
      throw new GenericException(ErrorCodeEnum.LABEL_NOT_FOUND, labelId);
    }
    try{
      if(!activityAssignLabelService.save(new ActivityAssignLabelPO(idInToken, activityId, labelId))) {
        throw new GenericException(ErrorCodeEnum.ACTIVITY_ADD_LABEL_FAILED, activityId, labelId);
      }
    } catch (DuplicateKeyException e) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_LABEL_ALREADY_EXISTS, activityId, labelId);
    }
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_REMOVE_LABEL_API_PATH)
  @Operation(
      summary = "Remove a label from an activity",
      description = "Remove a label from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Remove activity label failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void removeLabelFromActivity(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);

    var activityAssignLabelPO = activityAssignLabelService.getById(id);
    if (activityAssignLabelPO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_LABEL_NOT_FOUND, id);
    }
    permissionService.checkActivityOperationValidity(
        activityAssignLabelPO.getActivityId(), idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    if (!activityAssignLabelService.removeById(id)) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_REMOVE_LABEL_FAILED, activityAssignLabelPO.getActivityId(), id);
    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH)
  @Operation(
      summary = "Page labels of an activity",
      description = "Page labels of an activity by activity id",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Page labels of an activity failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
    @ApiResponse(
        description = "Message conversion error",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<ActivityAssignLabelVO> pageLabelsOfActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    var iPage = activityAssignLabelService.pageActivityLabelsByActivityId(activityId, page, size);
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(ActivityAssignLabelVO::new).toList());
  }

  @PostMapping(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
  @Operation(
      summary = "Add an assignee to an activity",
      description = "Add an assignee to an activity with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(
        description = "Add assignee to activity failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void addAssigneeToActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("assigneeId") Long assigneeId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    var UserPO = userService.getById(assigneeId);
    if (UserPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, assigneeId);
    }
    try {
      if(!activityDesignateAssigneeService.save(new ActivityDesignateAssigneePO(idInToken, activityId, assigneeId))) {
        throw new GenericException(ErrorCodeEnum.ACTIVITY_ADD_ASSIGNEE_FAILED, activityId, assigneeId);
      }
    } catch (DuplicateKeyException e) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_ASSIGNEE_ALREADY_EXISTS, activityId, assigneeId);
    }
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_REMOVE_ASSIGNEE_API_PATH)
  @Operation(
      summary = "Remove an assignee from an activity",
      description = "Remove an assignee from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Activity remove assignee failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void removeAssigneeFromActivity(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    var activityDesignateAssigneePO = activityDesignateAssigneeService.getById(id);
    if (activityDesignateAssigneePO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_ASSIGNEE_NOT_FOUND, id);
    }
    permissionService.checkActivityOperationValidity(
        activityDesignateAssigneePO.getActivityId(), idInToken, OperationTypeEnum.MODIFY_ACTIVITY);
    if (!activityDesignateAssigneeService.removeById(id)) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_REMOVE_ASSIGNEE_FAILED,
          activityDesignateAssigneePO.getActivityId(),
          id);
    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_ASSIGNEE_API_PATH)
  @Operation(
      summary = "Page assignees of an activity",
      description = "Page assignees of an activity by activity id",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Page assignees of an activity failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
    @ApiResponse(
        description = "Message conversion error",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<AssigneeVO> pageAssigneesOfActivity(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    permissionService.checkActivityOperationValidity(
        activityId,
        TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true),
        OperationTypeEnum.READ);
    var iPage =
        activityDesignateAssigneeService.pageActivityAssigneesByActivityId(activityId, page, size);
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(AssigneeVO::new).toList());
  }

  @GetMapping(ApiPathConstant.ACTIVITY_CHECK_OPERATION_VALIDITY_API_PATH)
  @Operation(
      summary = "Check if the operation on an activity is valid",
      description = "Check if the operation on an activity is valid",
      tags = {"Activity", "Get Method"})
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
  public void checkActivityOperationValidity(
          @RequestParam Long id,
          @RequestParam OperationTypeEnum operationType,
          @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkActivityOperationValidity(id, idInToken, operationType);
  }

  @GetMapping(ApiPathConstant.ACTIVITY_CHECK_COMMENT_OPERATION_VALIDITY_API_PATH)
  @Operation(
      summary = "Check if the operation on a comment is valid",
      description = "Check if the operation on a comment is valid",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void checkCommentOperationValidity(
      @RequestParam Long id,
      @RequestParam OperationTypeEnum operationType,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken), true);
    permissionService.checkCommentOperationValidity(id, idInToken, operationType);
  }

  public void checkSubIssueCreationValidity(Long subIssueRepositoryId, Long parentId) {
    var parentActivityPO = activityService.getById(parentId);
    // the parent activity cannot be pr
    if (parentActivityPO == null || parentActivityPO.getIsPullRequest()) {
      throw new GenericException(ErrorCodeEnum.WRONG_ISSUE_INFORMATION);
    }
    // The repository creators of the parent and sub activities must be the same
    var subIssueRepositoryPO = repositoryService.getById(subIssueRepositoryId);
    var parentRepositoryPO = repositoryService.getById(parentActivityPO.getRepositoryId());
    if (subIssueRepositoryPO == null
            || parentRepositoryPO == null
            || !parentRepositoryPO.getUserId().equals(subIssueRepositoryPO.getUserId())) {
      throw new GenericException(ErrorCodeEnum.SUB_ISSUE_CREATE_FAILED, subIssueRepositoryId);
    }
  }
}
