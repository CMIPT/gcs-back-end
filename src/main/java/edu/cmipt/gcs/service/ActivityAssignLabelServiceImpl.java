package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.ActivityAssignLabelMapper;
import edu.cmipt.gcs.pojo.label.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ActivityAssignLabelServiceImpl
    extends ServiceImpl<ActivityAssignLabelMapper, ActivityAssignLabelPO>
    implements ActivityAssignLabelService {
  private final ActivityAssignLabelMapper activityAssignLabelMapper;

  public ActivityAssignLabelServiceImpl(ActivityAssignLabelMapper activityAssignLabelMapper) {
    this.activityAssignLabelMapper = activityAssignLabelMapper;
  }

  @Override
  public ActivityAssignLabelPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean save(ActivityAssignLabelPO activityAssignLabelPO) {
    return super.save(activityAssignLabelPO);
  }

  @Override
  public boolean updateById(ActivityAssignLabelPO activityAssignLabelPO) {
    return super.updateById(activityAssignLabelPO);
  }

  @Override
  public boolean removeById(Serializable id) {
    return super.removeById(id);
  }

  @Override
  public List<Long> removeByActivityId(Serializable activityId) {
    return removeByActivityIds(List.of((Long) activityId));
  }

  @Override
  public Map<Long, List<LabelDTO>> getLabelsByActivityIds(List<Long> activityIds) {
    var queryWrapper =
        JoinWrappers.lambda(ActivityAssignLabelPO.class)
            .select(ActivityAssignLabelPO::getActivityId)
            .select(LabelPO::getId, LabelPO::getName, LabelPO::getDescription, LabelPO::getHexColor)
            .innerJoin(LabelPO.class, LabelPO::getId, ActivityAssignLabelPO::getLabelId)
            .in(ActivityAssignLabelPO::getActivityId, activityIds);
    List<ActivityOwnLabelDTO> activityAssignLabelPOS =
        activityAssignLabelMapper.selectJoinList(ActivityOwnLabelDTO.class, queryWrapper);

    return activityAssignLabelPOS.stream()
        .collect(
            Collectors.groupingBy(
                ActivityOwnLabelDTO::getActivityId,
                Collectors.mapping(
                    label ->
                        new LabelDTO(
                            label.getId().toString(),
                            null,
                            label.getName(),
                            label.getHexColor(),
                            label.getDescription()),
                    Collectors.toList())));
  }

  @Override
  public List<Long> removeByActivityIds(List<Long> activityIds) {
    List<Long> activityAssignLabelIds = super.list(
                    new QueryWrapper<ActivityAssignLabelPO>()
                            .select("id")
                            .in("activity_id", activityIds)
            ).stream()
            .map(ActivityAssignLabelPO::getId)
            .collect(Collectors.toList());
    super.remove(new QueryWrapper<ActivityAssignLabelPO>().in("activity_id", activityIds));
    return activityAssignLabelIds;
  }

  @Override
  public List<Long> removeByLabelId(Serializable LabelId) {
    List<Long> activityAssignLabelIds = super.list(
                    new QueryWrapper<ActivityAssignLabelPO>()
                            .select("id")
                            .eq("label_id", LabelId)
            ).stream()
            .map(ActivityAssignLabelPO::getId)
            .toList();
    super.remove(new QueryWrapper<ActivityAssignLabelPO>().eq("label_id", LabelId));
    return activityAssignLabelIds;
  }

  @Override
  public Page<ActivityAssignLabelDTO> pageActivityLabelsByActivityId(
      Long activityId, Integer pageNum, Integer pageSize) {
    Page<ActivityAssignLabelDTO> page = new Page<>(pageNum, pageSize);
    var queryWrapper =
        JoinWrappers.lambda(ActivityAssignLabelPO.class)
            .selectAsClass(ActivityAssignLabelPO.class, ActivityAssignLabelDTO.class)
            .selectAs(LabelPO::getId, ActivityAssignLabelDTO::getLabelId)
            .selectAs(LabelPO::getName, ActivityAssignLabelDTO::getLabelName)
            .selectAs(LabelPO::getDescription, ActivityAssignLabelDTO::getLabelDescription)
            .selectAs(LabelPO::getHexColor, ActivityAssignLabelDTO::getLabelHexColor)
            .selectAs(LabelPO::getGmtCreated, ActivityAssignLabelDTO::getGmtCreated)
            .innerJoin(LabelPO.class, LabelPO::getId, ActivityAssignLabelPO::getLabelId)
            .eq(ActivityAssignLabelPO::getActivityId, activityId);
    return activityAssignLabelMapper.selectJoinPage(
        page, ActivityAssignLabelDTO.class, queryWrapper);
  }

  @Override
  public ActivityAssignLabelPO getOneByActivityIdAndLabelId(Long activityId, Long labelId) {
    return super.getOne(
        new QueryWrapper<ActivityAssignLabelPO>()
            .eq("activity_id", activityId)
            .eq("label_id", labelId));
  }
}
