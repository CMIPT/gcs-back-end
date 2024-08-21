package edu.cmipt.gcs.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User Data Transfer Object")
public class UserDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "User ID")
    private Long id;
    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;
    @Schema(description = "Email", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin@cmipt.edu")
    private String email;
    @Schema(description = "User Password (Unencrypted)", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String userPassword;
}
