package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneePO;
import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ActivityDesignateAssigneeService extends IService<ActivityDesignateAssigneePO> {

  List<Long> removeByActivityId(Serializable activityId);

  Page<AssigneeDTO> pageActivityAssigneesByActivityId(
      Long activityId, Integer pageNum, Integer pageSize);

  ActivityDesignateAssigneePO getOneByActivityIdAndAssigneeId(Long activityId, Long assigneeId);

  Map<Long, List<AssigneeDTO>> getAssigneesByActivityIds(List<Long> activityIds);

  List<Long> removeByActivityIds(List<Long> activityIds);
}
