package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.SshKeyMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;
import edu.cmipt.gcs.util.GitoliteUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Service
public class SshKeyServiceImpl extends ServiceImpl<SshKeyMapper, SshKeyPO>
        implements SshKeyService {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    @Transactional
    @Override
    public boolean save(SshKeyPO sshKeyPO) {
        if (!super.save(sshKeyPO)) {
            logger.error("Failed to save SSH key to database");
            return false;
        }
        if (!GitoliteUtil.addSshKey(
                sshKeyPO.getId(), sshKeyPO.getPublicKey(), sshKeyPO.getUserId())) {
            logger.error("Failed to add SSH key to gitolite");
            throw new GenericException(ErrorCodeEnum.SSH_KEY_UPLOAD_FAILED, sshKeyPO);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        SshKeyPO sshKeyPO = super.getById(id);
        assert sshKeyPO != null;
        if (!super.removeById(id)) {
            logger.error("Failed to remove SSH key from database");
            return false;
        }
        if (!GitoliteUtil.removeSshKey(sshKeyPO.getId(), sshKeyPO.getUserId())) {
            logger.error("Failed to remove SSH key from gitolite");
            throw new GenericException(ErrorCodeEnum.SSH_KEY_DELETE_FAILED, sshKeyPO);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean updateById(SshKeyPO sshKeyPO) {
        String originSshKey = super.getById(sshKeyPO.getId()).getPublicKey();
        assert originSshKey != null;
        if (!super.updateById(sshKeyPO)) {
            logger.error("Failed to update SSH key in database");
            return false;
        }
        // no need to update file, we just return true
        if (sshKeyPO.getPublicKey() == null || originSshKey.equals(sshKeyPO.getPublicKey())) {
            return true;
        }
        GitoliteUtil.updateSshKey(sshKeyPO.getId(), sshKeyPO.getPublicKey());
        return true;
    }
}
