package edu.cmipt.gcs.pojo.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import edu.cmipt.gcs.util.MD5Converter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("t_user")
public class UserPO {
    private Long id;
    private String username;
    private String email;
    private String userPassword;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreated;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtUpdated;

    @TableLogic private LocalDateTime gmtDeleted;

    public UserPO(UserSignUpDTO user) {
        this.username = user.username();
        this.email = user.email();
        this.userPassword = MD5Converter.convertToMD5(user.userPassword());
    }

    public UserPO(UserUpdateDTO user) {
        this.id = Long.parseLong(user.id());
        this.username = user.username();
    }
}
