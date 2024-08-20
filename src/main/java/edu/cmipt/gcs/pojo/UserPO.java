package edu.cmipt.gcs.pojo;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_user")
public record UserPO(@TableId Long pkUserId, String username, String email, String userPassword, LocalDateTime gmtCreated,
        LocalDateTime gmtUpdated, LocalDateTime gmtDeleted) {
}
