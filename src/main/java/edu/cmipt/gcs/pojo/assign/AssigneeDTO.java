package edu.cmipt.gcs.pojo.assign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
