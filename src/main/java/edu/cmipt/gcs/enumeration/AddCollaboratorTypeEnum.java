package edu.cmipt.gcs.enumeration;

import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.TypeConversionUtil;

public enum AddCollaboratorTypeEnum {
  ID,
  EMAIL,
  USERNAME;

  public UserPO getOne(UserService service, String collaborator) {
    switch (this) {
      case ID:
        return service.getById(TypeConversionUtil.convertToLong(collaborator, true));
      case USERNAME:
        return service.getOneByUsername(collaborator);
      case EMAIL:
        return service.getOneByEmail(collaborator);
    }
    return null;
  }
}
