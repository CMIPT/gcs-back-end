package edu.cmipt.gcs.pojo.user;

import edu.cmipt.gcs.constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * User Data Transfer Object
 *
 * @author Kaiser
 */
@Schema(description = "User Update Data Transfer Object")
public record UserUpdateDTO(
    @Schema(
            description = "Username",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "admin")
        @Size(
            min = ValidationConstant.MIN_USERNAME_LENGTH,
            max = ValidationConstant.MAX_USERNAME_LENGTH)
        @Pattern(regexp = ValidationConstant.USERNAME_PATTERN)
        String username,
    @Schema(
            description = "Avatar URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "https://www.example.com/avatar.jpg")
        @Size(
            min = ValidationConstant.MIN_AVATAR_URL_LENGTH,
            max = ValidationConstant.MAX_AVATAR_URL_LENGTH)
        @URL
        String avatarUrl) {}
