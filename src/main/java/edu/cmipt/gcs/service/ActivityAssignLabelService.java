package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelDTO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ActivityAssignLabelService extends IService<ActivityAssignLabelPO> {
  Page<ActivityAssignLabelDTO> pageActivityLabelsByActivityId(
      Long activityId, Integer pageNum, Integer pageSize);

  ActivityAssignLabelPO getOneByActivityIdAndLabelId(Long activityId, Long labelId);

  void removeByActivityId(Serializable id);

  Map<Long, List<LabelVO>> getLabelsByActivityIds(List<Long> activityIds);

  void removeByActivityIds(List<Long> activityIds);

  void removeByLabelId(Serializable LabelId);
}
