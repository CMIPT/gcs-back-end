package edu.cmipt.gcs.service;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.OperationTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
  private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

  @Autowired private ActivityService activityService;
  @Autowired private RepositoryService repositoryService;
  @Autowired private UserCollaborateRepositoryService userCollaborateRepositoryService;

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
      // only repository creator and collaborators have permission to modify the repository
      else if (operationTypeEnum == OperationTypeEnum.MODIFY) {
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
    // If the activity is locked, the permission to create comment operations is downgraded to
    // modify operations
    // and only the repository creator and collaborators can perform comment operations.
    if (operationTypeEnum == OperationTypeEnum.COMMENT && activityPO.getGmtLocked() != null) {
      operationTypeEnum = OperationTypeEnum.MODIFY;
    }
    // whatever the repository is public or private,
    // the corresponding repository must be visible to activity creator,
    // so we just need check if idInToken is equal to one of activity creator id ,repository creator
    // id, repository collaborator id
    if (!idInToken.equals(activityPO.getUserId())) {
      Long repositoryId = activityPO.getRepositoryId();
      checkRepositoryOperationValidity(repositoryId, idInToken, operationTypeEnum);
    }
  }

  @Override
  public void checkIssueOperationValidity(Long subIssueRepositoryId, Long parentId) {
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
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED, subIssueRepositoryId);
    }
  }
}
