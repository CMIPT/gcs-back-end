package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cmipt.gcs.constant.ApiPathConstant;
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
import edu.cmipt.gcs.pojo.label.LabelVO;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@Tag(name = "Activity",description = "Activity Related API")
public class ActivityController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired private UserService userService;
    @Autowired private ActivityService activityService;
    @Autowired private RepositoryService repositoryService;
    @Autowired private CommentService commentService;
    @Autowired private UserCollaborateRepositoryService userCollaborateRepositoryService;
    @Autowired private ActivityAssignLabelService activityAssignLabelService;
    @Autowired private ActivityDesignateAssigneeService activityDesignateAssigneeService;
    @Autowired private LabelService labelService;

    // TODO 查询问题子问题


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
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        Long repositoryId = null;
        try {
            repositoryId = Long.valueOf(activity.repositoryId());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        checkRepositoryOperationValidity(
                repositoryId,
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId));
        ActivityPO latestActivityPO= activityService.getLatestActivityByRepositoryId(repositoryId);
        int activityNumber;
        if( latestActivityPO == null) {
            activityNumber = 1;
        } else {
            activityNumber = latestActivityPO.getNumber() + 1;
        }
        var activityPO = new ActivityPO(activity,activityNumber, idInToken.toString());
        if (!activityService.save(activityPO)) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_CREATE_FAILED, activity);
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
        var activityPO = activityService.getById(id);
        if (activityPO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, id);
        }
        String userId = JwtUtil.getId(accessToken);
        // only admin can delete activity
        if (!userId.equals(activityPO.getUserId().toString())) {
            logger.info(
                    "User[{}] tried to delete activity of user[{}]", userId, activityPO.getUserId());
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (!activityService.removeById(id)) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_DELETE_FAILED, id);
        }
    }

    // 包括修改状态（关闭，锁）
    @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_API_PATH)
    @Operation(
            summary = "Update an activity",
            description = "Update an activity with the given information",
            tags = {"Activity", "Post Method"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(
                    description = "Update activity failed",
                    content = @Content(schema = @Schema(implementation = ErrorVO.class))),
    })
    public void updateActivity(
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId));
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
            tags = {"Activity", "Get Method"})
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
        checkRepositoryOperationValidity(
                repositoryId,
                Long.valueOf(JwtUtil.getId(accessToken)),
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId));
        var iPage = activityService.pageActivities(activityQueryDTO,new Page<>(page,size));
        return new PageVO<>(
                iPage.getTotal(), iPage.getRecords().stream().map(ActivityDetailVO::new).toList());
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
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "activityNumber",required = false) Long activityNumber,
            @RequestParam(value = "repositoryId",required = false) Long repositoryId,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        ActivityPO activityPO;
        if(id == null)
        {
            if(activityNumber == null || repositoryId == null)
                throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
            activityPO = activityService.getOneByActivityNumberAndRepositoryId(activityNumber, repositoryId);
        }
        else
        {
            activityPO = activityService.getById(id);
        }
        String notFoundMessage = id != null ? id.toString() : repositoryId + "/" + activityNumber;
        if (activityPO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage);
        }
        id = activityPO.getId();
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        checkActivityOperationValidity(
                id,
                idInToken,
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage));
        ActivityDetailDTO activityDetailDTO = activityService.getDetailedOneById(id);
        if (activityDetailDTO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, notFoundMessage);
        }
        return new ActivityDetailVO(activityDetailDTO);
    }

    @GetMapping(ApiPathConstant.ACTIVITY_GET_COMMENT_API_PATH)
    @Operation(
            summary = "Get an activity comment",
            description = "Get an activity comment by activity id and comment id",
            tags = {"Activity", "Get Method"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(
                    description = "Comment not found",
                    content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public CommentVO getActivityComment(
            @RequestParam("activityId") Long activityId,
            @RequestParam("commentId") Long commentId,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, commentId));
        CommentPO commentPO = commentService.getById(commentId);
        if (commentPO == null) {
            throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, commentId);
        }
        return new CommentVO(commentPO);
    }

    //修改评论(内容 或者隐藏状态)
    @PostMapping(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_API_PATH)
    @Operation(
            summary = "Update an activity comment",
            description = "Update an activity comment with the given information",
            tags = {"Activity", "Post Method"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(
                    description = "Update comment failed",
                    content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void updateActivityComment(
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
        checkActivityOperationValidity(
                commentPO.getActivityId(),
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, commentId));
        if(!commentService.updateById(new CommentPO(comment, idInToken)))
        {
            throw new GenericException(ErrorCodeEnum.COMMENT_UPDATE_FAILED, comment);
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
        checkActivityOperationValidity(
                commentPO.getActivityId(),
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, id));
        if(!commentService.removeById(id)) {
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, comment.activityId()));
        if(!commentService.save(new CommentPO(comment, idInToken))) {
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId));
        var wrapper = new QueryWrapper<CommentPO>();
        wrapper.eq("activity_id", activityId);
        wrapper.orderBy(true, true, "gmt_created");
        var iPage = commentService.page(
                new Page<>(page, size), wrapper);
        return new PageVO<>(
                iPage.getTotal(),
                iPage.getRecords().stream()
                        .map(CommentVO::new)
                        .toList());
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId));
        // 检查这个标签是否存在于当前活动对应的仓库中
        var activityPO = activityService.getById(activityId);
        if (activityPO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId);
        }
        Long repositoryId = activityPO.getRepositoryId();
        var LabelPO = labelService.getById(labelId);
        if( LabelPO == null || !repositoryId.equals(LabelPO.getRepositoryId())) {
            throw new GenericException(ErrorCodeEnum.LABEL_NOT_FOUND, labelId);
        }
       if( activityAssignLabelService.getOneByActivityIdAndLabelId(activityId, labelId) != null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_LABEL_ALREADY_EXISTS, activityId, labelId);
        }
        if (!activityAssignLabelService.save(new ActivityAssignLabelPO(idInToken,activityId, labelId))) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_ADD_LABEL_FAILED, activityId,labelId);
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
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityAssignLabelPO.getActivityId());
        }
        checkRepositoryOperationValidity(
                activityPO.getRepositoryId(),
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACCESS_DENIED));
        if (!activityAssignLabelService.removeById(id)) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_DELETE_LABEL_FAILED, activityPO.getId(), id);
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND,activityId));
        var iPage = activityAssignLabelService.pageActivityLabelsByActivityId(
                activityId, new Page<>(page, size));
        return new PageVO<>(
                iPage.getTotal(),
                iPage.getRecords().stream()
                        .map(ActivityAssignLabelVO::new)
                        .toList());
    }

    @PostMapping(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
    @Operation(
            summary = "Add an assignee to an activity",
            description = "Add an assignee to an activity with the given information",
            tags = {"Activity", "Post Method"})
    @ApiResponses({
            @ApiResponse
                    (description = "Add assignee to activity failed",
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
        checkActivityOperationValidity(
                activityId,
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId));
        if(activityDesignateAssigneeService.getOneByActivityIdAndAssigneeId(activityId, assigneeId) != null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_ASSIGNEE_ALREADY_EXISTS, activityId, assigneeId);
        }
        if(!activityDesignateAssigneeService.save(new ActivityDesignateAssigneePO(
                idInToken, activityId, assigneeId))) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_ADD_ASSIGNEE_FAILED, activityId, assigneeId);
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
        checkActivityOperationValidity(
                activityDesignateAssigneePO.getActivityId(),
                idInToken,
                OperationTypeEnum.WRITE,
                new GenericException(ErrorCodeEnum.ACTIVITY_ASSIGNEE_NOT_FOUND,id));
        if(!activityDesignateAssigneeService.removeById(id)) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_DELETE_ASSIGNEE_FAILED, activityDesignateAssigneePO.getActivityId(),id);
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
        checkActivityOperationValidity(
                activityId,
                Long.valueOf(JwtUtil.getId(accessToken)),
                OperationTypeEnum.READ,
                new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND,activityId));
        var iPage = activityDesignateAssigneeService.pageActivityAssigneesByActivityId(
                activityId, new Page<>(page, size));
        return new PageVO<>(
                iPage.getTotal(),
                iPage.getRecords().stream()
                        .map(AssigneeVO::new)
                        .toList());
    }




    /**
     * Check the validity of the repository operation.
     *
     * @param repositoryId the repository id
     * @param userId the user id
     * @param e the message when the repository is not found
     * @throws GenericException if the repository is private and the user is not the creator or is not
     *     one of collaborators
     */
    private void checkRepositoryOperationValidity(Long repositoryId, Long userId, OperationTypeEnum operationTypeEnum, GenericException e) {
        // Check if the repository exists
        var repositoryPO = repositoryService.getById(repositoryId);
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
        }
        // If the repository is private, we return NOT_FOUND to make sure the user can't know
        if (!userId.equals(repositoryPO.getUserId())
                && userCollaborateRepositoryService.getOneByCollaboratorIdAndRepositoryId(
                userId, repositoryPO.getId())
                == null) {
            logger.debug(
                    "User[{}] tried to get repository of user[{}]", userId, repositoryPO.getUserId());
            if(repositoryPO.getIsPrivate())
                throw e;
            else if(operationTypeEnum == OperationTypeEnum.WRITE) {
                throw new GenericException(ErrorCodeEnum.ACCESS_DENIED, repositoryId);
            }
        }
    }

    private void checkActivityOperationValidity(
            Long activityId,
            Long idInToken,
            OperationTypeEnum operationTypeEnum,
            GenericException e) {
        var activityPO = activityService.getById(activityId);
        if (activityPO == null) {
            throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId);
        }
        Long repositoryId = activityPO.getRepositoryId();
        checkRepositoryOperationValidity(repositoryId, idInToken,operationTypeEnum, e);
    }

}
