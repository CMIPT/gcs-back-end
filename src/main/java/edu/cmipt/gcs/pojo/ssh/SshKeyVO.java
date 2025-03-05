package edu.cmipt.gcs.pojo.ssh;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SSH Key Value Object")
public record SshKeyVO(String id, String userId, String name, String publicKey) {
  public SshKeyVO(SshKeyPO sshKeyPO) {
    this(
        sshKeyPO.getId().toString(),
        sshKeyPO.getUserId().toString(),
        sshKeyPO.getName(),
        sshKeyPO.getPublicKey());
  }
}
