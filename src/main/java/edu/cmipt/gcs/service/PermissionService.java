package edu.cmipt.gcs.service;

import edu.cmipt.gcs.enumeration.OperationTypeEnum;
import edu.cmipt.gcs.exception.GenericException;

public interface PermissionService {
  /**
   * Check the validity of the repository operation.
   *
   * @param repositoryId the repository id
   * @param userId the user id
   * @param operationTypeEnum the operation type
   * @throws GenericException if the repository is private and the user is not the creator or is not
   *     one of collaborators
   */
  void checkRepositoryOperationValidity(
      Long repositoryId, Long userId, OperationTypeEnum operationTypeEnum);

  /**
   * Check the validity of the activity operation.
   *
   * @param activityId the activity id
   * @param userId the user id
   * @param operationTypeEnum the operation type
   * @throws GenericException if activity not found or check repository operation validity failed
   */
  void checkActivityOperationValidity(
      Long activityId, Long userId, OperationTypeEnum operationTypeEnum);
}
