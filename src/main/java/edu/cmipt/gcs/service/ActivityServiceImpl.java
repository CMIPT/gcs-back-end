package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.ActivityMapper;
import edu.cmipt.gcs.dao.CommentMapper;
import edu.cmipt.gcs.pojo.activity.*;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneePO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.LabelPO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.user.UserPO;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, ActivityPO>
    implements ActivityService {

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ActivityServiceImpl.class);

  @Autowired private ActivityMapper activityMapper;
  @Autowired private ActivityAssignLabelService activityAssignLabelService;
  @Autowired private ActivityDesignateAssigneeService activityDesignateAssigneeService;
  @Autowired private CommentService commentService;
  @Autowired private CommentMapper commentMapper;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Override
  public ActivityPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean updateById(ActivityPO activity) {
    return super.updateById(activity);
  }

  @Override
  public boolean save(ActivityPO activity) {
    return super.save(activity);
  }

  @Override
  @Transactional
  public boolean removeById(Serializable id) {
    // 删除活动时，删除活动相关的信息（标签、指定的参与者、评论等）
    activityAssignLabelService.removeByActivityId(id);
    activityDesignateAssigneeService.removeByActivityId(id);
    commentService.removeByActivityId(id);

    // 删除活动
    return super.removeById(id);
  }

  @Override
  public ActivityPO getLatestActivityByRepositoryId(Long repositoryId) {
    return activityMapper.getLatestActivityIgnoreLogicDeleted(repositoryId);
  }

  @Override
  public Page<ActivityDetailDTO> pageActivities(
      ActivityQueryDTO activityQueryDTO, Page<ActivityDetailDTO> page) {
    // 连表分页查询
    var queryWrapper =
        JoinWrappers.lambda(ActivityPO.class)
            .selectAsClass(ActivityPO.class, ActivityDetailDTO.class)
            .selectAs(RepositoryPO::getId, ActivityDetailDTO::getRepositoryId)
            .selectAs(UserPO::getUsername, ActivityDetailDTO::getUsername)
            .leftJoin(UserPO.class, UserPO::getId, ActivityPO::getUserId)
            .leftJoin(RepositoryPO.class, RepositoryPO::getId, ActivityPO::getRepositoryId)
            .eq(RepositoryPO::getId, Long.valueOf(activityQueryDTO.repositoryId()))
            .eq(ActivityPO::getIsPullRequest, activityQueryDTO.isPullRequest());

    if (activityQueryDTO.labels() != null && !activityQueryDTO.labels().isEmpty()) {
      queryWrapper.in(
          ActivityPO::getId,
          ActivityAssignLabelPO.class,
          sub ->
              sub.select(ActivityAssignLabelPO::getActivityId)
                  .leftJoin(LabelPO.class, LabelPO::getId, ActivityAssignLabelPO::getLabelId)
                  .in(LabelPO::getName, activityQueryDTO.labels()));
    }
    if (activityQueryDTO.assignees() != null && !activityQueryDTO.assignees().isEmpty()) {
      queryWrapper.in(
          ActivityPO::getId,
          ActivityDesignateAssigneePO.class,
          sub ->
              sub.select(ActivityDesignateAssigneePO::getActivityId)
                  .leftJoin(UserPO.class, UserPO::getId, ActivityDesignateAssigneePO::getAssigneeId)
                  .in(UserPO::getUsername, activityQueryDTO.assignees()));
    }
    if (activityQueryDTO.author() != null)
      queryWrapper.eq(UserPO::getUsername, activityQueryDTO.author());
    if (activityQueryDTO.isClosed() != null && activityQueryDTO.isClosed()) {
      queryWrapper.isNotNull(ActivityPO::getGmtClosed);
    }
    if (activityQueryDTO.isLocked() != null && activityQueryDTO.isLocked()) {
      queryWrapper.isNotNull(ActivityPO::getGmtLocked);
    }
    if (activityQueryDTO.orderBy() != null) {
      switch (activityQueryDTO.orderBy()) {
        case GMT_CREATED:
          queryWrapper.orderBy(true, activityQueryDTO.isAsc(), ActivityPO::getGmtCreated);
          break;
        case GMT_UPDATED:
          queryWrapper.orderBy(true, activityQueryDTO.isAsc(), ActivityPO::getGmtUpdated);
          break;
          //                case TOTAL_COMMENTS:
          //                    queryWrapper.orderBy(true, activityQueryDTO.isAsc(),
          //                            ActivityDetailDTO::getCommentCnt);
          //                    break; // todo 暂不支持按评论数排序
      }
    }
    Page<ActivityDetailDTO> activityDetailDTOPage =
        activityMapper.selectJoinPage(page, ActivityDetailDTO.class, queryWrapper);
    logger.info("total activities: {}", activityDetailDTOPage.getTotal());
    logger.info("record activities: {}", activityDetailDTOPage.getRecords());
    if (activityDetailDTOPage.getRecords().isEmpty()) return activityDetailDTOPage;
    // 提取ActivityPO的ID列表
    List<Long> activityIds =
        activityDetailDTOPage.getRecords().stream().map(ActivityDetailDTO::getId).toList();
    logger.info("activityIds: {}", activityIds);
    //  批量查询每个Activity的标签、指定的参与者和评论数
    Map<Long, List<LabelVO>> labelMap =
        activityAssignLabelService.getLabelsByActivityIds(activityIds);
    Map<Long, List<AssigneeVO>> assigneeMap =
        activityDesignateAssigneeService.getAssigneesByActivityIds(activityIds);
    Map<Long, Long> commentCountMap = commentService.getCommentCountByActivityIds(activityIds);

    // 将查询结果填充到ActivityDetailDTO中
    page.getRecords()
        .forEach(
            activity -> {
              activity.setLabels(labelMap.getOrDefault(activity.getId(), Collections.emptyList()));
              activity.setAssignees(
                  assigneeMap.getOrDefault(activity.getId(), Collections.emptyList()));
              activity.setCommentCnt(commentCountMap.getOrDefault(activity.getId(), 0L));
            });
    return activityDetailDTOPage;
  }

  @Override
  public ActivityDetailDTO getDetailedOneById(Long id) {
    var queryWrapper =
        JoinWrappers.lambda(ActivityPO.class)
            .selectAsClass(ActivityPO.class, ActivityDetailDTO.class)
            .selectAs(RepositoryPO::getId, ActivityDetailDTO::getRepositoryId)
            .selectAs(UserPO::getUsername, ActivityDetailDTO::getUsername)
            .leftJoin(UserPO.class, UserPO::getId, ActivityPO::getUserId)
            .leftJoin(RepositoryPO.class, RepositoryPO::getId, ActivityPO::getRepositoryId)
            .eq(ActivityPO::getId, id);

    ActivityDetailDTO activityDetailDTO =
        activityMapper.selectJoinOne(ActivityDetailDTO.class, queryWrapper);
    if (activityDetailDTO != null) {
      Long commentCount =
          commentMapper.selectCount(
              Wrappers.lambdaQuery(CommentPO.class).eq(CommentPO::getActivityId, id));
      List<LabelVO> labelDTOList =
          activityAssignLabelService
              .getLabelsByActivityIds(List.of(id))
              .getOrDefault(id, Collections.emptyList());
      List<AssigneeVO> assigneeDTOList =
          activityDesignateAssigneeService
              .getAssigneesByActivityIds(List.of(id))
              .getOrDefault(id, Collections.emptyList());

      activityDetailDTO.setCommentCnt(commentCount);
      activityDetailDTO.setLabels(labelDTOList);
      activityDetailDTO.setAssignees(assigneeDTOList);
    }
    return activityDetailDTO;
  }

  @Override
  public ActivityPO getOneByActivityNumberAndRepositoryId(Long activityNumber, Long repositoryId) {
    return super.getOne(
        new QueryWrapper<ActivityPO>()
            .eq("number", activityNumber)
            .eq("repository_id", repositoryId));
  }

  @Override
  @Transactional
  public void removeByRepositoryId(Long repositoryId) {
    // 获取所有活动ID
    List<Long> activityIds =
        super.list(new QueryWrapper<ActivityPO>().select("id").eq("repository_id", repositoryId))
            .stream()
            .map(ActivityPO::getId)
            .toList();
    if (!activityIds.isEmpty()) {
      // 删除活动相关的信息（标签、指定的参与者、评论等）
      activityAssignLabelService.removeByActivityIds(activityIds);
      activityDesignateAssigneeService.removeByActivityIds(activityIds);
      commentService.removeByActivityIds(activityIds);
    }

    // 删除活动
    super.remove(new QueryWrapper<ActivityPO>().eq("repository_id", repositoryId));
  }
}
