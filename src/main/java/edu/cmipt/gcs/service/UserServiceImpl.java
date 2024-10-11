package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.util.GitoliteUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired SshKeyService sshKeyService;

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        if (!super.removeById(id)) {
            logger.error("Failed to remove user from database");
            return false;
        }
        QueryWrapper<SshKeyPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        var sshKeyList = sshKeyService.list(wrapper);
        for (var sshKey : sshKeyList) {
            if (!sshKeyService.removeById(sshKey.getId())) {
                throw new GenericException(
                        ErrorCodeEnum.USER_DELETE_FAILED,
                        "Failed to remove user ssh key: {}",
                        sshKey);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean save(UserPO user) {
        if (!super.save(user)) {
            logger.error("Failed to save user to database");
            return false;
        }
        if (!GitoliteUtil.initUserConfig(user.getId())) {
            logger.error("Failed to add user to gitolite");
            throw new GenericException(
                    ErrorCodeEnum.USER_CREATE_FAILED, "Failed to add user to gitolite");
        }
        return true;
    }
}
