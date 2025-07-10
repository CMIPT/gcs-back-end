package edu.cmipt.gcs.pojo.issue;

import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Issue Data Transfer Object")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueDTO {
  private Long id; // activity id
  private Integer number;
  private Long repositoryId;
  private String repositoryName;
  private String title;
  private String description;
  private String username;
  private List<AssigneeDTO> assignees;
  private Timestamp gmtClosed;
  private Long subIssueTotalCount;
  private Long subIssueCompletedCount;
}
