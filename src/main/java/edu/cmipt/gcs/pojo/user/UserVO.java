package edu.cmipt.gcs.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Value Object")
public record UserVO(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Username", example = "admin") String username,
        @Schema(description = "Email", example = "admin@cmipt.edu") String email) {
    public UserVO(UserPO userPO) {
        this(
                userPO.getId(),
                userPO.getUsername(),
                userPO.getEmail());
    }
}
