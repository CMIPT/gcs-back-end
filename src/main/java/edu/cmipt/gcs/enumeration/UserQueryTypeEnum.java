package edu.cmipt.gcs.enumeration;

import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.JwtUtil;

public enum UserQueryTypeEnum {
  USERNAME,
  EMAIL,
  ID,
  TOKEN;

  public UserPO getOne(UserService service, String user) {
    switch (this) {
      case ID:
        try {
          return service.getById(Long.valueOf(user));
        } catch (Exception e) {
          throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
      case TOKEN:
        Long idInToken = Long.valueOf(JwtUtil.getId(user));
        return service.getById(idInToken);
      case USERNAME:
        return service.getOneByUsername(user);
      case EMAIL:
        return service.getOneByEmail(user);
    }
    return null;
  }
}
