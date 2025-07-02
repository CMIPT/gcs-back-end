package edu.cmipt.gcs.pojo.ssh;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Timestamp;

import edu.cmipt.gcs.util.TypeConversionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public SshKeyPO(SshKeyDTO sshKeyDTO, String userId) {
    this(
        null,
        null,
        sshKeyDTO.name(),
        sshKeyDTO.publicKey(),
        null,
        null,
        null
    );
    this.id = TypeConversionUtil.convertToLong(sshKeyDTO.id());
    this.userId = TypeConversionUtil.convertToLong(userId);
  }

  public SshKeyPO(SshKeyDTO sshKeyDTO) {
    this(sshKeyDTO, null);
  }
}
