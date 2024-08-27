package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.util.JwtUtil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Value Object")
public record UserVO(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Username", example = "admin") String username,
        @Schema(description = "Email", example = "admin@cmipt.edu") String email,
        @Schema(description = "Access Token") String accessToken,
        @Schema(description = "Refresh Token") String refreshToken) {
    public UserVO(UserPO userPO) {
        this(
                userPO.getId(),
                userPO.getUsername(),
                userPO.getEmail(),
                JwtUtil.generateToken(userPO.getId(), TokenTypeEnum.ACCESS_TOKEN),
                JwtUtil.generateToken(userPO.getId(), TokenTypeEnum.REFRESH_TOKEN));
    }
}
