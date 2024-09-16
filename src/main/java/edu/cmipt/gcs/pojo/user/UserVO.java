package edu.cmipt.gcs.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Value Object")
public record UserVO(
        // The Long can not be expressed correctly in json, so use String instead
        @Schema(description = "User ID") String id,
        @Schema(description = "Username", example = "admin") String username,
        @Schema(description = "Email", example = "admin@cmipt.edu") String email) {
    public UserVO(UserPO userPO) {
        this(userPO.getId().toString(), userPO.getUsername(), userPO.getEmail());
    }
}
