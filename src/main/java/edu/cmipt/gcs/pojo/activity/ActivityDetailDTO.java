package edu.cmipt.gcs.pojo.activity;

import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailDTO {
    private Long id; // activity id
    private Integer number;
    private Long repositoryId;
    private String title;
    private String description;
    private String username;
    private List<LabelVO> labels;
    private List<AssigneeVO> assignees;
    private Long commentCnt;
    private Timestamp gmtCreated;
    private Timestamp gmtClosed;
    private Timestamp gmtLocked;
}
