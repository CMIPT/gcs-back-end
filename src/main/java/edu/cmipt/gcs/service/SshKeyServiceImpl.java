package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.constant.GitConstant;
import edu.cmipt.gcs.dao.SshKeyMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
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
        saveSshKeyToAuthorizedKeys(sshKeyPO.getPublicKey());
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
        removeSshKeyFromAuthorizedKeys(sshKeyPO.getPublicKey());
        return true;
    }

    @Transactional
    @Override
    public boolean updateById(SshKeyPO sshKeyPO) {
        String originSshKey = super.getById(sshKeyPO.getId()).getPublicKey();
        assert originSshKey != null;
        if (!super.updateById(sshKeyPO)) {
            return false;
        }
        // no need to update file, we just return true
        if (sshKeyPO.getPublicKey() == null || originSshKey.equals(sshKeyPO.getPublicKey())) {
            return true;
        }
        // remove the origin ssh key and save the new ssh key
        removeSshKeyFromAuthorizedKeys(originSshKey);
        saveSshKeyToAuthorizedKeys(sshKeyPO.getPublicKey());
        return true;
    }

    private void saveSshKeyToAuthorizedKeys(String sshKey) {
        try {
            ProcessBuilder sshKeySaver =
                    new ProcessBuilder(
                            "sudo",
                            "-u",
                            GitConstant.GIT_USER_NAME,
                            "tee",
                            "-a",
                            GitConstant.GIT_HOME_DIRECTORY + "/.ssh/authorized_keys");
            // for singleton, we can use synchronized(this) to lock the object
            synchronized (this) {
                Process process = sshKeySaver.start();
                try (OutputStream os = process.getOutputStream()) {
                    os.write((GitConstant.SSH_KEY_PREFIX + sshKey + '\n').getBytes());
                    os.flush();
                }
                if (process.waitFor() != 0) {
                    logger.error("Failed to write SSH key to authorized_keys file");
                    throw new GenericException(
                            ErrorCodeEnum.SSH_KEY_UPLOAD_FAILED,
                            process.errorReader().lines().toList().toString());
                }
            }
        } catch (Exception e) {
            // rollback the database operation
            throw new GenericException(ErrorCodeEnum.SSH_KEY_UPLOAD_FAILED, e.getMessage());
        }
    }

    private void removeSshKeyFromAuthorizedKeys(String sshKey) {
        try {
            ProcessBuilder sshKeyRemover =
                    new ProcessBuilder(
                            "sudo",
                            "-u",
                            GitConstant.GIT_USER_NAME,
                            "sed",
                            "-i",
                            "/^" + GitConstant.SSH_KEY_PREFIX + sshKey + "$/d",
                            GitConstant.GIT_HOME_DIRECTORY + "/.ssh/authorized_keys");
            synchronized (this) {
                Process process = sshKeyRemover.start();
                if (process.waitFor() != 0) {
                    logger.error("Failed to remove SSH key from authorized_keys");
                    throw new GenericException(
                            ErrorCodeEnum.SSH_KEY_DELETE_FAILED,
                            process.errorReader().lines().toList().toString());
                }
            }
        } catch (Exception e) {
            // rollback the database operation
            throw new GenericException(ErrorCodeEnum.SSH_KEY_DELETE_FAILED, e.getMessage());
        }
    }
}
