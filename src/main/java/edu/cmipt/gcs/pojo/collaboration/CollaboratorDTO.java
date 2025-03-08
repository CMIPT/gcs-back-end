package edu.cmipt.gcs.pojo.collaboration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaboratorDTO {
  private Long id;
  private Long collaboratorId;
  private String username;
  private String email;
  private String avatarUrl;
}
