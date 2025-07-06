package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.comment.CommentVO;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.service.*;
import edu.cmipt.gcs.util.JwtUtil;
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

import java.sql.Timestamp;

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

  // TODO 查询问题子问题
  // TODO 查询评论子评论

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
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    Long repositoryId = null;
    try {
      repositoryId = Long.valueOf(activity.repositoryId());
    } catch (NumberFormatException e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
    }
    permissionService.checkRepositoryOperationValidity(
        repositoryId, idInToken, OperationTypeEnum.WRITE);
    // 检查父活动是否存在
    if(activity.parentId()!=null)
    {
      Long parentId = null;
        try {
            parentId = Long.valueOf(activity.parentId());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        var parentActivityPO = activityService.getById(parentId);
        if (parentActivityPO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, parentId);
        }
    }
    int retry = 0;
    while (true) {
      try {
        ActivityPO latestActivityPO = activityService.getLatestActivityByRepositoryId(repositoryId);
        int number = (latestActivityPO == null ? 1 : latestActivityPO.getNumber() + 1);
        var activityPO = new ActivityPO(activity, number, idInToken.toString());
        activityService.save(activityPO);
        return;
      } catch (DuplicateKeyException e) {
        retry++;
        if (retry >= ApplicationConstant.CREATE_LABEL_MAX_RETRY_TIMES) {
          throw new GenericException(ErrorCodeEnum.ACTIVITY_CREATE_FAILED, activity);
        }
        // 短暂 sleep 重试
        Thread.sleep(50);
      }
    }
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

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_LOCK_STATE_API_PATH)
    @Operation(
        summary = "Update an activity lock state",
        description = "Update an activity lock state with the given information",
        tags = {"Activity", "Post Method"})
    @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(
          description = "Update activity lock state failed",
          content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void updateActivityLockState(
        @RequestParam("id") Long id,
        @RequestParam("is locked") Boolean isLocked,
        @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(
        id, idInToken, OperationTypeEnum.WRITE);
    var activityPO = activityService.getById(id);
    LambdaUpdateWrapper<ActivityPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(ActivityPO::getId, id);
    if(!isLocked) {
      updateWrapper.set(ActivityPO::getGmtClosed, null);;
    }
    else if(activityPO.getGmtClosed() == null) {
      updateWrapper.set(ActivityPO::getGmtClosed, new Timestamp(System.currentTimeMillis()));
    }
    else {
      return; // 如果活动已经被锁定，则不需要更新
    }
    if(!activityService.update(updateWrapper)) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CLOSE_STATE_API_PATH)
    @Operation(
        summary = "Update an activity close state",
        description = "Update an activity close state with the given information",
        tags = {"Activity", "Post Method"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(
            description = "Update activity close state failed",
            content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void updateActivityCloseState(
        @RequestParam("id") Long id,
        @RequestParam("is closed") Boolean isClosed,
        @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(
        id, idInToken, OperationTypeEnum.WRITE);
    var activityPO = activityService.getById(id);
    LambdaUpdateWrapper<ActivityPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(ActivityPO::getId, id);
    if(!isClosed) {
      updateWrapper.set(ActivityPO::getGmtClosed, null);;
    }
    else if(activityPO.getGmtClosed() == null) {
      updateWrapper.set(ActivityPO::getGmtClosed, new Timestamp(System.currentTimeMillis()));
    }
    else {
      return; // 如果活动已经被关闭，则不需要更新
    }
    if(!activityService.update(updateWrapper)) {
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

    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    Long activityId = null;
    try {
      activityId = Long.valueOf(activity.id());
    } catch (NumberFormatException e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
    }
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.WRITE);
    // 检查父活动是否存在
    if(activity.parentId()!=null)
    {
      Long parentId = null;
      try {
        parentId = Long.valueOf(activity.parentId());
      } catch (NumberFormatException e) {
        logger.error(e.getMessage());
        throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
      }
      var parentActivityPO = activityService.getById(parentId);
      if (parentActivityPO == null) {
        throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, parentId);
      }
    }
    if (!activityService.updateById(new ActivityPO(activity))) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_UPDATE_FAILED, activityId);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH)
  @Operation(
      summary = "Page a repository's activities",
      description =
          "Page a repository's activities. If the given token is trying to get other repository's"
              + " activities, only public repository's activities will be returned",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "User activities page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<ActivityDetailVO> pageActivity(
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @Validated(QueryGroup.class) @RequestBody ActivityQueryDTO activityQueryDTO,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long repositoryId = null;
    try {
      repositoryId = Long.valueOf(activityQueryDTO.repositoryId());
    } catch (NumberFormatException e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
    }
    String user = activityQueryDTO.user();
    var userPO = activityQueryDTO.userType().getOne(userService, user);
    if (userPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, user);
    }
    permissionService.checkRepositoryOperationValidity(
        repositoryId, Long.valueOf(JwtUtil.getId(accessToken)), OperationTypeEnum.READ);
    var iPage = activityService.pageActivities(activityQueryDTO, new Page<>(page, size));
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(ActivityDetailVO::new).toList());
  }

  @PostMapping(ApiPathConstant.ACTIVITY_GET_SUB_ACTIVITY_API_PATH)
    @Operation(
        summary = "Page an activity's sub-activities",
        description =
            "Page an activity's sub-activities. If the given token is trying to get other activity's"
                + " sub-activities, only public activity's sub-activities will be returned",
        tags = {"Activity", "Post Method"})
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "User activities page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public PageVO<ActivityDetailVO> pageSubActivity(
        @RequestParam("page") @Min(1) Integer page,
        @RequestParam("size") @Min(1) Integer size,
        @Validated(QueryGroup.class) @RequestBody ActivityQueryDTO activityQueryDTO,
        @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
      return pageActivity(page, size, activityQueryDTO, accessToken);
    }

  @GetMapping(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
  @Operation(
      summary = "Get an activity detail",
      description = "Get an activity detail by id or activity number and repository title",
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
  public ActivityDetailVO getActivityDetail(
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
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(id, idInToken, OperationTypeEnum.READ);
    ActivityDetailDTO activityDetailDTO = activityService.getDetailedOneById(id);
    if (activityDetailDTO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage);
    }
    return new ActivityDetailVO(activityDetailDTO);
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_CONTENT_API_PATH)
  @Operation(
      summary = "Update an activity comment content",
      description = "Update an activity comment content with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update comment content failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateActivityCommentContent(
      @Validated(UpdateGroup.class) @RequestBody CommentDTO comment,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    Long commentId = null;
    try {
      commentId = Long.valueOf(comment.id());
    } catch (NumberFormatException e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
    }
    var commentPO = commentService.getById(commentId);
    if (commentPO == null) {
      throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, commentId);
    }
    permissionService.checkActivityOperationValidity(
        commentPO.getActivityId(), idInToken, OperationTypeEnum.WRITE);
    if (!commentService.updateById(new CommentPO(comment, idInToken))) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, comment);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
    @Operation(
        summary = "Update an activity comment hidden state",
        description = "Update an activity comment hidden state with the given information",
        tags = {"Activity", "Post Method"})
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Update comment hidden state failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void updateActivityCommentHiddenState(
        @RequestParam("id") Long id,
        @RequestParam("isHidden") Boolean isHidden,
        @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    var commentPO = commentService.getById(id);
    if (commentPO == null) {
      throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, id);
    }
    permissionService.checkActivityOperationValidity(
        commentPO.getActivityId(), idInToken, OperationTypeEnum.WRITE);
    LambdaUpdateWrapper<CommentPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(CommentPO::getId, id);
    if(!isHidden) {
      updateWrapper.set(CommentPO::getGmtHidden, null);;
    }
    else if(commentPO.getGmtHidden() == null) {
      updateWrapper.set(CommentPO::getGmtHidden, new Timestamp(System.currentTimeMillis()));
    }
    else {
      return; // 如果评论已经被隐藏，则不需要更新
    }
    if(!commentService.update(updateWrapper)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
  @Operation(
      summary = "Update an activity comment resolved state",
      description = "Update an activity comment resolved state with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
  @ApiResponse(responseCode = "200", description = "Success"),
  @ApiResponse(
      description = "Update comment resolved state failed",
      content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateActivityCommentResolvedState(
      @RequestParam("id") Long id,
      @RequestParam("isResolved") Boolean isResolved,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    var commentPO = commentService.getById(id);
    if (commentPO == null) {
      throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, id);
    }
    permissionService.checkActivityOperationValidity(
        commentPO.getActivityId(), idInToken, OperationTypeEnum.WRITE);
    LambdaUpdateWrapper<CommentPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(CommentPO::getId, id);
    if(!isResolved) {
      updateWrapper.set(CommentPO::getGmtResolved, null);;
    }
    else if(commentPO.getGmtResolved() == null) {
      updateWrapper.set(CommentPO::getGmtResolved, new Timestamp(System.currentTimeMillis()));
    }
    else {
      return; // 如果评论已经被解决，则不需要更新
    }
    if(!commentService.update(updateWrapper)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, id);
    }
}

  @DeleteMapping(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
  @Operation(
      summary = "Delete an activity comment",
      description = "Delete an activity comment with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Comment delete failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteActivityComment(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    CommentPO commentPO = commentService.getById(id);
    if (commentPO == null) {
      throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, id);
    }
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(
        commentPO.getActivityId(), idInToken, OperationTypeEnum.WRITE);
    if (!commentService.removeById(id)) {
      throw new GenericException(ErrorCodeEnum.COMMENT_DELETE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
  @Operation(
      summary = "Create an activity comment",
      description = "Create an activity comment with the given information",
      tags = {"Activity", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Create comment failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
  })
  public void createActivityComment(
      @Validated(CreateGroup.class) @RequestBody CommentDTO comment,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    Long activityId = null;
    try {
      activityId = Long.valueOf(comment.activityId());
    } catch (NumberFormatException e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
    }
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.WRITE);
    if(comment.parentId()!=null) {
      // 检查评论的父评论是否存在
      Long parentId = null;
      try {
        parentId = Long.valueOf(comment.parentId());
      } catch (NumberFormatException e) {
        logger.error(e.getMessage());
        throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
      }
      var parentCommentPO = commentService.getById(parentId);
      if (parentCommentPO == null) {
          throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, parentId);
      }
    }
    if (!commentService.save(new CommentPO(comment, idInToken))) {
      throw new GenericException(ErrorCodeEnum.COMMENT_CREATE_FAILED, comment);
    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_COMMENT_API_PATH)
  @Operation(
      summary = "Page activity comments",
      description = "Page comments of an activity",
      tags = {"Activity", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Activity not found",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CommentVO> pageActivityComment(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {

    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    var wrapper = new QueryWrapper<CommentPO>();
    wrapper.eq("activity_id", activityId);
    wrapper.isNull("parent_id"); // 默认查询根评论,子评论通过另外的 API查询
    wrapper.orderBy(true, true, "gmt_created");
    var iPage = commentService.page(new Page<>(page, size), wrapper);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(CommentVO::new).toList());
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_SUB_COMMENT_API_PATH)
  @Operation(
          summary = "Page activity sub-comments",
          description = "Page sub-comments of an activity",
          tags = {"Activity", "Get Method"})
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Success"),
          @ApiResponse(
                  description = "Activity not found",
                  content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<CommentVO> pageActivitySubComment(
          @RequestParam("activityId") Long activityId,
          @RequestParam("parentId") Long parentId,
          @RequestParam("page") @Min(1) Integer page,
          @RequestParam("size") @Min(1) Integer size,
          @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {

    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    var wrapper = new QueryWrapper<CommentPO>();
    wrapper.eq("activity_id", activityId);
    wrapper.eq("parent_id", parentId); // 查询子评论
    wrapper.orderBy(true, true, "gmt_created");
    var iPage = commentService.page(new Page<>(page, size), wrapper);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(CommentVO::new).toList());
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
  public void addActivityLabel(
      @RequestParam("activityId") Long activityId,
      @RequestParam("labelId") Long labelId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.WRITE);
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
    if (activityAssignLabelService.getOneByActivityIdAndLabelId(activityId, labelId) != null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_LABEL_ALREADY_EXISTS, activityId, labelId);
    }
    if (!activityAssignLabelService.save(
        new ActivityAssignLabelPO(idInToken, activityId, labelId))) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_ADD_LABEL_FAILED, activityId, labelId);
    }
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_DELETE_LABEL_API_PATH)
  @Operation(
      summary = "Delete a label from an activity",
      description = "Delete a label from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Delete activity label failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteActivityLabel(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));

    var activityAssignLabelPO = activityAssignLabelService.getById(id);
    if (activityAssignLabelPO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_LABEL_NOT_FOUND, id);
    }
    var activityPO = activityService.getById(activityAssignLabelPO.getActivityId());
    if (activityPO == null) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityAssignLabelPO.getActivityId());
    }
    permissionService.checkRepositoryOperationValidity(
        activityPO.getRepositoryId(), idInToken, OperationTypeEnum.WRITE);
    if (!activityAssignLabelService.removeById(id)) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_DELETE_LABEL_FAILED, activityPO.getId(), id);
    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH)
  @Operation(
      summary = "Get labels of an activity",
      description = "Get labels of an activity by activity id",
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
  public PageVO<ActivityAssignLabelVO> pageActivityLabels(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    permissionService.checkActivityOperationValidity(activityId, idInToken, OperationTypeEnum.READ);
    var iPage =
        activityAssignLabelService.pageActivityLabelsByActivityId(
            activityId, new Page<>(page, size));
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
  public void addActivityAssignee(
      @RequestParam("activityId") Long activityId,
      @RequestParam("assigneeId") Long assigneeId,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    var UserPO = userService.getById(assigneeId);
    if (UserPO == null) {
      throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, assigneeId);
    }
    permissionService.checkActivityOperationValidity(
        activityId, idInToken, OperationTypeEnum.WRITE);
    if (activityDesignateAssigneeService.getOneByActivityIdAndAssigneeId(activityId, assigneeId)
        != null) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_ASSIGNEE_ALREADY_EXISTS, activityId, assigneeId);
    }
    if (!activityDesignateAssigneeService.save(
        new ActivityDesignateAssigneePO(idInToken, activityId, assigneeId))) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_ADD_ASSIGNEE_FAILED, activityId, assigneeId);
    }
  }

  @DeleteMapping(ApiPathConstant.ACTIVITY_DELETE_ASSIGNEE_API_PATH)
  @Operation(
      summary = "Delete an assignee from an activity",
      description = "Delete an assignee from an activity with the given id",
      tags = {"Activity", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(
        description = "Activity assignee not found",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteActivityAssignee(
      @RequestParam("id") Long id,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
    var activityDesignateAssigneePO = activityDesignateAssigneeService.getById(id);
    if (activityDesignateAssigneePO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_ASSIGNEE_NOT_FOUND, id);
    }
    permissionService.checkActivityOperationValidity(
        activityDesignateAssigneePO.getActivityId(), idInToken, OperationTypeEnum.WRITE);
    if (!activityDesignateAssigneeService.removeById(id)) {
      throw new GenericException(
          ErrorCodeEnum.ACTIVITY_DELETE_ASSIGNEE_FAILED,
          activityDesignateAssigneePO.getActivityId(),
          id);
    }
  }

  @GetMapping(ApiPathConstant.ACTIVITY_PAGE_ASSIGNEE_API_PATH)
  @Operation(
      summary = "Get assignees of an activity",
      description = "Get assignees of an activity by activity id",
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
  public PageVO<AssigneeVO> pageActivityAssignees(
      @RequestParam("activityId") Long activityId,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    permissionService.checkActivityOperationValidity(
        activityId, Long.valueOf(JwtUtil.getId(accessToken)), OperationTypeEnum.READ);
    var iPage =
        activityDesignateAssigneeService.pageActivityAssigneesByActivityId(
            activityId, new Page<>(page, size));
    return new PageVO<>(
        iPage.getTotal(), iPage.getRecords().stream().map(AssigneeVO::new).toList());
  }
}
