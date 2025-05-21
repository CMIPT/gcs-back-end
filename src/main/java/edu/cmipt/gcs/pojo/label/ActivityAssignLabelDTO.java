package edu.cmipt.gcs.pojo.label;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Activity Assign Label Data Transfer Object")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAssignLabelDTO{
    @Schema(description = "Primary Key ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "12345")
    private Long userId;

    @Schema(description = "Label ID", example = "67890")
    private Long labelId;

    @Schema(description = "Label Name", example = "bug")
    private String labelName;

    @Schema(description = "Label Description", example = "Indicates a bug in the system")
    private String labelDescription;

    @Schema(description = "Label Hex Color", example = "#FF5733")
    private String labelHexColor;

}
