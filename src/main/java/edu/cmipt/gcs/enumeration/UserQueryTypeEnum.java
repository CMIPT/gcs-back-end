package edu.cmipt.gcs.enumeration;

import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.TypeConversionUtil;

public enum UserQueryTypeEnum {
  USERNAME,
  EMAIL,
  ID,
  TOKEN;

  public UserPO getOne(UserService service, String user) {
    switch (this) {
      case ID:
        try {
          return service.getById(TypeConversionUtil.convertToLong(user,true));
        } catch (Exception e) {
          throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
      case TOKEN:
        Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(user),true);
        return service.getById(idInToken);
      case USERNAME:
        return service.getOneByUsername(user);
      case EMAIL:
        return service.getOneByEmail(user);
    }
    return null;
  }
}
