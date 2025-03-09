package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cmipt.gcs.dao.SshKeyMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;
import edu.cmipt.gcs.util.GitoliteUtil;
import edu.cmipt.gcs.util.RedisUtil;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SshKeyServiceImpl extends ServiceImpl<SshKeyMapper, SshKeyPO>
    implements SshKeyService {
  private static final Logger logger = LoggerFactory.getLogger(SshKeyServiceImpl.class);

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Override
  public SshKeyPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public SshKeyPO getOneByUserIdAndName(Long userId, String name) {
    return super.getOne(new QueryWrapper<SshKeyPO>().eq("user_id", userId).eq("name", name));
  }

  @Override
  public SshKeyPO getOneByUserIdAndPublicKey(Long userId, String publicKey) {
    return super.getOne(
        new QueryWrapper<SshKeyPO>().eq("user_id", userId).eq("public_key", publicKey));
  }

  @Transactional
  @Override
  public boolean save(SshKeyPO sshKeyPO) {
    if (!super.save(sshKeyPO)) {
      logger.error("Failed to save SSH key to database");
      return false;
    }
    if (!GitoliteUtil.addSshKey(sshKeyPO.getId(), sshKeyPO.getPublicKey(), sshKeyPO.getUserId())) {
      logger.error("Failed to add SSH key to gitolite");
      throw new GenericException(ErrorCodeEnum.SSH_KEY_UPLOAD_FAILED, sshKeyPO);
    }
    return true;
  }

  @Transactional
  @Override
  public boolean removeById(Serializable id) {
    var sshKeyPO =
        (SshKeyPO) redisTemplate.opsForValue().get(RedisUtil.generateKey(this, id.toString()));
    if (sshKeyPO == null) {
      sshKeyPO = super.getById(id);
    }
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
    if (!super.updateById(sshKeyPO)) {
      logger.error("Failed to update SSH key in database");
      return false;
    }
    GitoliteUtil.updateSshKey(sshKeyPO.getId(), sshKeyPO.getPublicKey());
    return true;
  }
}
