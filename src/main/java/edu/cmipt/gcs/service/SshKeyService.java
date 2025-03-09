package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;

public interface SshKeyService extends IService<SshKeyPO> {
  SshKeyPO getOneByUserIdAndName(Long userId, String name);

  SshKeyPO getOneByUserIdAndPublicKey(Long userId, String publicKey);
}
