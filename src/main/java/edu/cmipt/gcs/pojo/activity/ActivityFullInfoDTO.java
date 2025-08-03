package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import edu.cmipt.gcs.pojo.label.LabelDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import java.sql.Timestamp;
import java.util.List;
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
  private UserPO creator;
  private UserPO modifier;
  private List<LabelDTO> labels;
  private List<AssigneeDTO> assignees;
  private Long commentCnt;
  private Timestamp gmtCreated;
  private Timestamp gmtUpdated;
  private Timestamp gmtClosed;
  private Timestamp gmtLocked;
}
