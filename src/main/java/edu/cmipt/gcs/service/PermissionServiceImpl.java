package edu.cmipt.gcs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.OperationTypeEnum;
import edu.cmipt.gcs.exception.GenericException;

/**
 * 权限检查服务实现类
 * 该类负责检查用户对仓库、活动和评论的操作权限。
 * 
 * 读取权限：
 * 如果仓库是public的，则任何用户都可以读取仓库、活动和评论。
 * 如果仓库是private的，则只有仓库创建者和协作者可以读取仓库、活动和评论。
 * 
 * 修改权限：
 * 对于仓库，只有仓库创建者和协作者可以修改仓库组件，目前只有标签组件。
 * 对于活动，仓库创建者和协作者可以修改活动，如果活动所属仓库是public的，则活动创建者也可以修改活动。
 * 对于评论，仓库创建者和协作者可以修改评论，如果评论所属活动是未锁定的并且活动所属仓库是public的，则评论创建者也可以修改评论。
 */
@Service
public class PermissionServiceImpl implements PermissionService {
  private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

  @Autowired private ActivityService activityService;
  @Autowired private RepositoryService repositoryService;
  @Autowired private UserCollaborateRepositoryService userCollaborateRepositoryService;
  @Autowired private CommentService commentService;

  @Override
  public void checkRepositoryOperationValidity(
      Long repositoryId, Long userId, OperationTypeEnum operationTypeEnum) {
    // Check if the repository exists
    var repositoryPO = repositoryService.getById(repositoryId);
    if (repositoryPO == null) {
      throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
    }
    if (!userId.equals(repositoryPO.getUserId())
        && userCollaborateRepositoryService.getOneByCollaboratorIdAndRepositoryId(
                userId, repositoryPO.getId())
            == null) {
      logger.debug(
          "User[{}] tried to get repository of user[{}]", userId, repositoryPO.getUserId());
      // If the repository is private, we return NOT_FOUND to make sure the user can't know
      if (repositoryPO.getIsPrivate()) {
        throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, repositoryId);
      }
      // Only repository creator and collaborators have permission to modify the repository
      else if (operationTypeEnum == OperationTypeEnum.MODIFY_REPOSITORY) {
        throw new GenericException(ErrorCodeEnum.ACCESS_DENIED, repositoryId);
      }
    }
  }

  @Override
  public void checkActivityOperationValidity(
      Long activityId, Long idInToken, OperationTypeEnum operationTypeEnum) {
    var activityPO = activityService.getById(activityId);
    if (activityPO == null) {
      throw new GenericException(ErrorCodeEnum.ACTIVITY_NOT_FOUND, activityId);
    }
    Long repositoryId = activityPO.getRepositoryId();
    if (operationTypeEnum == OperationTypeEnum.READ || operationTypeEnum == OperationTypeEnum.ATTACH_ACTIVITY) {
      checkRepositoryOperationValidity(repositoryId, idInToken, operationTypeEnum);
    } 
    else {
      try {
        checkRepositoryOperationValidity(repositoryId, idInToken, OperationTypeEnum.MODIFY_REPOSITORY);
        } catch (GenericException e) {
          // If it's not ACCESS_DENIED, throw directly
          if (!ErrorCodeEnum.ACCESS_DENIED.equals(e.getCode())) {
            throw e;
          }
          // It's ACCESS_DENIED, handle specially based on operation type
          if (operationTypeEnum == OperationTypeEnum.MODIFY_ACTIVITY && idInToken.equals(activityPO.getCreatorId())) {
              // Activity creator is allowed to modify activity, don't throw exception
              return;
          }

          // Check comment operation validity based on activity lock status
          if(activityPO.getGmtLocked() != null) {
            // Activity is locked, throw ACTIVITY_LOCKED exception, so that only repository creator and collaborators can modify activity
            throw new GenericException(ErrorCodeEnum.ACTIVITY_LOCKED, activityId);
          }

          if(operationTypeEnum == OperationTypeEnum.ATTACH_COMMENT) {
            // Activity is not locked, any user can attach comment
            return;
          }
          // Other cases(MODIFY_COMMENT), no special handling, continue to throw exception
          throw e;
        }
    }
  }

  @Override
  public void checkCommentOperationValidity(
      Long commentId, Long idInToken, OperationTypeEnum operationTypeEnum) {
    var commentPO = commentService.getById(commentId);
    if (commentPO == null) {
      throw new GenericException(ErrorCodeEnum.COMMENT_NOT_FOUND, commentId);
    }
    
    try {
      checkActivityOperationValidity(commentPO.getActivityId(), idInToken, operationTypeEnum);
            } catch (GenericException e) {
        if (ErrorCodeEnum.ACCESS_DENIED.equals(e.getCode()) && idInToken.equals(commentPO.getCreatorId())) {
          // When ACCESS_DENIED exception occurs, if user is comment creator, allow to modify comment
          return;
        } 
        // Re-throw other types of exceptions (including ACTIVITY_LOCKED)
        throw e;
      }
  }
}
