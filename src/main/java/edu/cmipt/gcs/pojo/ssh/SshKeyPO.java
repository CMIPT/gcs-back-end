package edu.cmipt.gcs.pojo.ssh;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("t_ssh_key")
@NoArgsConstructor
public class SshKeyPO {
    private Long id;
    private Long userId;
    private String name;
    private String publicKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreated;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtUpdated;

    @TableLogic private LocalDateTime gmtDeleted;

    public SshKeyPO(SshKeyDTO sshKeyDTO, String userId) {
        try {
            this.id = Long.valueOf(sshKeyDTO.id());
        } catch (NumberFormatException e) {
            this.id = null;
        }
        try {
            this.userId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            this.userId = null;
        }
        this.name = sshKeyDTO.name();
        this.publicKey = sshKeyDTO.publicKey();
    }

    public SshKeyPO(SshKeyDTO sshKeyDTO) {
        this(sshKeyDTO, null);
    }
}
