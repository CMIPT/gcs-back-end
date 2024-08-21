package edu.cmipt.gcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("t_user")
public record UserPO(
        @TableId Long pkUserId,
        String username,
        String email,
        String userPassword,
        LocalDateTime gmtCreated,
        LocalDateTime gmtUpdated,
        LocalDateTime gmtDeleted) {}
