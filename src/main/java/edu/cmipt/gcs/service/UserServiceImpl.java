package edu.cmipt.gcs.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.pojo.user.UserPO;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {
}
