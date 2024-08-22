package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.pojo.user.UserPO;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {}
