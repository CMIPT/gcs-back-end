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
    @TableLogic
    private LocalDateTime gmtDeleted;

    public UserPO(UserDTO userDTO) {
        this.id = userDTO.getId();
        this.username = userDTO.getUsername();
        this.email = userDTO.getEmail();
        this.userPassword = MD5Converter.convertToMD5(userDTO.getUserPassword());
    }
}
