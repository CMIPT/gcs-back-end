package edu.cmipt.gcs.pojo.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Issue Count Data Transfer Object")
public class IssueCountDTO {
    @Schema(description = "Sub Issue Count")
    private Long count;

    @Schema(description = "Issue ID")
    private Long issueId;
}
