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
     * @param e the message when the repository is not found
     * @throws GenericException if the repository is private and the user is not the creator or is not
     *     one of collaborators
     */
    void checkRepositoryOperationValidity(
            Long repositoryId, Long userId, OperationTypeEnum operationTypeEnum, GenericException e);

    void checkActivityOperationValidity(
            Long activityId, Long userId, OperationTypeEnum operationTypeEnum, GenericException e);
}
