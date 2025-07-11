package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelDTO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.LabelDTO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ActivityAssignLabelService extends IService<ActivityAssignLabelPO> {
  Page<ActivityAssignLabelDTO> pageActivityLabelsByActivityId(
      Long activityId, Integer pageNum, Integer pageSize);

  List<Long> removeByActivityId(Serializable id);

  Map<Long, List<LabelDTO>> getLabelsByActivityIds(List<Long> activityIds);

  List<Long> removeByActivityIds(List<Long> activityIds);

  List<Long> removeByLabelId(Serializable LabelId);
}
