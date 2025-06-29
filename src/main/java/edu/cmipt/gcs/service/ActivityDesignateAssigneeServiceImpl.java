package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.ActivityDesignateAssigneeMapper;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneeDTO;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneePO;
import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.user.UserPO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityDesignateAssigneeServiceImpl
    extends ServiceImpl<ActivityDesignateAssigneeMapper, ActivityDesignateAssigneePO>
    implements ActivityDesignateAssigneeService {
  @Autowired private ActivityDesignateAssigneeMapper activityDesignateAssigneeMapper;

  @Override
  public ActivityDesignateAssigneePO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean save(ActivityDesignateAssigneePO activityDesignateAssigneePO) {
    return super.save(activityDesignateAssigneePO);
  }

  @Override
  public boolean updateById(ActivityDesignateAssigneePO activityDesignateAssigneePO) {
    return super.updateById(activityDesignateAssigneePO);
  }

  @Override
  public boolean removeById(Serializable id) {
    return super.removeById(id);
  }

  @Override
  public void removeByActivityId(Serializable activityId) {
    super.remove(new QueryWrapper<ActivityDesignateAssigneePO>().eq("activity_id", activityId));
  }

  @Override
  public Page<AssigneeDTO> pageActivityAssigneesByActivityId(
      Long activityId, Page<AssigneeDTO> page) {
    var queryWrapper =
        JoinWrappers.lambda(ActivityDesignateAssigneePO.class)
            .selectAsClass(ActivityDesignateAssigneePO.class, AssigneeDTO.class)
            .selectAs(UserPO::getUsername, AssigneeDTO::getUsername)
            .selectAs(UserPO::getAvatarUrl, AssigneeDTO::getAvatarUrl)
            .selectAs(UserPO::getEmail, AssigneeDTO::getEmail)
            .innerJoin(UserPO.class, UserPO::getId, ActivityDesignateAssigneePO::getAssigneeId)
            .eq(ActivityDesignateAssigneePO::getActivityId, activityId);
    return activityDesignateAssigneeMapper.selectJoinPage(page, AssigneeDTO.class, queryWrapper);
  }

  @Override
  public ActivityDesignateAssigneePO getOneByActivityIdAndAssigneeId(
      Long activityId, Long assigneeId) {
    return super.getOne(
        new QueryWrapper<ActivityDesignateAssigneePO>()
            .eq("activity_id", activityId)
            .eq("assignee_id", assigneeId));
  }

  @Override
  public Map<Long, List<AssigneeVO>> getAssigneesByActivityIds(List<Long> activityIds) {
    var queryWrapper =
        JoinWrappers.lambda(ActivityDesignateAssigneePO.class)
            .selectAsClass(ActivityDesignateAssigneePO.class, AssigneeDTO.class)
            .selectAs(UserPO::getUsername, AssigneeDTO::getUsername)
            .selectAs(UserPO::getAvatarUrl, AssigneeDTO::getAvatarUrl)
            .selectAs(UserPO::getEmail, AssigneeDTO::getEmail)
            .innerJoin(UserPO.class, UserPO::getId, ActivityDesignateAssigneePO::getAssigneeId)
            .in(ActivityDesignateAssigneePO::getActivityId, activityIds);

    List<ActivityDesignateAssigneeDTO> assigneeDTOs =
        activityDesignateAssigneeMapper.selectJoinList(
            ActivityDesignateAssigneeDTO.class, queryWrapper);

    return assigneeDTOs.stream()
        .collect(
            Collectors.groupingBy(
                ActivityDesignateAssigneeDTO::getActivityId,
                Collectors.mapping(
                    dto ->
                        new AssigneeVO(
                            dto.getId().toString(),
                            dto.getAssigneeId().toString(),
                            dto.getUsername(),
                            dto.getAvatarUrl(),
                            dto.getEmail()),
                    Collectors.toList())));
  }

  @Override
  public void removeByActivityIds(List<Long> activityIds) {
    super.remove(new QueryWrapper<ActivityDesignateAssigneePO>().in("activity_id", activityIds));
  }
}
