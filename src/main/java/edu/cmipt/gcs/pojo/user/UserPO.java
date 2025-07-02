package edu.cmipt.gcs.pojo.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.cmipt.gcs.util.MD5Converter;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@TableName("t_user")
@NoArgsConstructor
public class UserPO {
  private Long id;
  private String username;
  private String email;
  private String userPassword;
  private String avatarUrl;

  @TableField(fill = FieldFill.INSERT)
  private Timestamp gmtCreated;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Timestamp gmtUpdated;

  @TableLogic private Timestamp gmtDeleted;

  public UserPO(UserCreateDTO user) {
    this(
        null,
        user.username(),
        user.email(),
        MD5Converter.convertToMD5(user.userPassword()),
        null,
        null,
        null,
        null);
  }

  public UserPO(UserUpdateDTO user, Long id) {
    this(id, user.username(), null, null, user.avatarUrl(), null, null, null);
  }
}
