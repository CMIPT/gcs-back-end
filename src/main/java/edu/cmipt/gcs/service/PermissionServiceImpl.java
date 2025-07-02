package edu.cmipt.gcs.service;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.OperationTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import java.util.Objects;
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
      } else if (operationTypeEnum == OperationTypeEnum.WRITE) {
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
    // whatever the repository is public or private,
    // the corresponding repository must be visible to activity creator,
    // so we just need check if idInToken is equal to one of activity creator id ,repository creator
    // id, repository collaborator id
    if (!Objects.equals(idInToken, activityPO.getUserId())) {
      Long repositoryId = activityPO.getRepositoryId();
      checkRepositoryOperationValidity(repositoryId, idInToken, operationTypeEnum);
    }
  }
}
