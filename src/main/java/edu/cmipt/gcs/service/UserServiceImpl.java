package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;
import edu.cmipt.gcs.pojo.user.UserPO;

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
        sshKeyService.list(wrapper).forEach(sshKey -> sshKeyService.removeById(sshKey.getId()));
        return true;
    }
}
