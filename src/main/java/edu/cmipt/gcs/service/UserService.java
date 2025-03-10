package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.user.UserPO;

public interface UserService extends IService<UserPO> {
  UserPO getOneByUsername(String username);

  UserPO getOneByEmail(String email);
}
