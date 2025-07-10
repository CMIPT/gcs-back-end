package edu.cmipt.gcs.pojo.issue;

import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record IssueVO(
    @Schema(description = "id") String id,
    @Schema(description = "Activity Number") String number,
    @Schema(description = "Repository Id") String repositoryId,
    @Schema(description = "Repository Name") String repositoryName,
    @Schema(description = "Activity Title") String title,
    @Schema(description = "Activity Description") String description,
    @Schema(description = "Username") String username,
    @Schema(description = "Activity Assignees") List<AssigneeVO> assignees,
    @Schema(description = "Closed timestamp, seconds since epoch") String gmtClosed,
    @Schema(description = "Sub Issue Total Count") String subIssueTotalCount,
    @Schema(description = "Sub Issue Completed Count") String subIssueCompletedCount) {
  public IssueVO(IssueDTO issueDTO) {
    this(
        issueDTO.getId().toString(),
        issueDTO.getNumber().toString(),
        issueDTO.getRepositoryId().toString(),
        issueDTO.getRepositoryName(),
        issueDTO.getTitle(),
        issueDTO.getDescription(),
        issueDTO.getUsername(),
        issueDTO.getAssignees().stream().map(AssigneeVO::new).toList(),
        String.valueOf(issueDTO.getGmtClosed()),
        issueDTO.getSubIssueTotalCount().toString(),
        issueDTO.getSubIssueCompletedCount().toString());
  }
}
