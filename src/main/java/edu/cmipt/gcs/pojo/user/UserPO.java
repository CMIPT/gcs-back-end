package edu.cmipt.gcs.pojo.user;

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
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtUpdated;
    @TableLogic private LocalDateTime gmtDeleted;

    public UserPO(UserDTO userDTO) {
        try {
            this.id = Long.valueOf(userDTO.id());
        } catch (NumberFormatException e) {
            this.id = null;
        }
        this.username = userDTO.username();
        this.email = userDTO.email();
        this.userPassword = MD5Converter.convertToMD5(userDTO.userPassword());
    }
}
