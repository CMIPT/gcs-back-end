package edu.cmipt.gcs.pojo.assign;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeDTO {
  private Long id;
  private Long activityId;
  private Long assigneeId;
  private String username;
  private String email;
  private String avatarUrl;
  private Timestamp gmtCreated;
}
