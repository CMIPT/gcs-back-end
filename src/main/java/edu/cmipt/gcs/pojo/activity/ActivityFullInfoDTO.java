package edu.cmipt.gcs.pojo.activity;

import java.sql.Timestamp;
import java.util.List;

import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import edu.cmipt.gcs.pojo.label.LabelDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityFullInfoDTO {
  private Long id; // activity id
  private Integer number;
  private Long repositoryId;
  private String repositoryName;
  private String title;
  private String description;
  private Long creatorId;
  private String creatorUsername;
  private Long modifierId;
  private String modifierUsername;
  private String modifierEmail;
  private String modifierAvatarUrl;
  private List<LabelDTO> labels;
  private List<AssigneeDTO> assignees;
  private Long commentCnt;
  private Timestamp gmtCreated;
  private Timestamp gmtUpdated;
  private Timestamp gmtClosed;
  private Timestamp gmtLocked;
}
