package edu.cmipt.gcs.pojo.label;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Activity Own Label Data Transfer Object")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOwnLabelDTO {
    @Schema(description = "activity ID")
    private Long activityId;

    @Schema(description = "Label ID")
    private Long id;

    @Schema(description = "Label Name")
    private String name;

    @Schema(description = "Label Description")
    private String description;

    @Schema(description = "Label Color")
    private String hexColor;
}
