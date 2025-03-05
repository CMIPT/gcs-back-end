package edu.cmipt.gcs.enumeration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.util.JwtUtil;

public enum UserQueryTypeEnum {
  USERNAME,
  EMAIL,
  ID,
  TOKEN;

  public QueryWrapper<UserPO> getQueryWrapper(String user, String accessToken) {
    QueryWrapper<UserPO> wrapper = new QueryWrapper<>();
    switch (this) {
      case ID:
        if (user == null) {
          throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        try {
          Long id = Long.valueOf(user);
          wrapper.eq("id", id);
        } catch (Exception e) {
          throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        break;
      case TOKEN:
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        wrapper.eq("id", idInToken);
        break;
      case USERNAME, EMAIL:
        wrapper.apply("LOWER(" + this.name().toLowerCase() + ") = LOWER({0})", user);
        break;
    }
    return wrapper;
  }
}
