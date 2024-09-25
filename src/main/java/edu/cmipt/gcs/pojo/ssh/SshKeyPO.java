package edu.cmipt.gcs.pojo.ssh;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("t_ssh_key")
public class SshKeyPO {
    private Long id;
    private Long userId;
    private String name;
    private String publicKey;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtUpdated;
    @TableLogic private LocalDateTime gmtDeleted;

    public SshKeyPO(SshKeyDTO sshKeyDTO) {
        try {
            this.id = Long.valueOf(sshKeyDTO.id());
        } catch (NumberFormatException e) {
            this.id = null;
        }
        try {
            this.userId = Long.valueOf(sshKeyDTO.userId());
        } catch (NumberFormatException e) {
            this.userId = null;
        }
        this.name = sshKeyDTO.name();
        this.publicKey = sshKeyDTO.publicKey();
    }
}
