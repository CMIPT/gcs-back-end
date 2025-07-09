package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.ActivityMapper;
import edu.cmipt.gcs.dao.CommentMapper;
import edu.cmipt.gcs.pojo.activity.*;
import edu.cmipt.gcs.pojo.assign.ActivityDesignateAssigneePO;
import edu.cmipt.gcs.pojo.assign.AssigneeDTO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.issue.IssueCountDTO;
import edu.cmipt.gcs.pojo.issue.IssueDTO;
import edu.cmipt.gcs.pojo.label.ActivityAssignLabelPO;
import edu.cmipt.gcs.pojo.label.LabelDTO;
import edu.cmipt.gcs.pojo.label.LabelPO;
import edu.cmipt.gcs.pojo.label.LabelVO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.user.UserPO;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.cmipt.gcs.util.TypeConversionUtil;
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
  public Page<ActivityDetailDTO> pageActivitiesDetail(
      ActivityQueryDTO activityQueryDTO, Integer pageNum, Integer pageSize) {
    Page<ActivityDetailDTO> page = new Page<>(pageNum, pageSize);
    // 连表分页查询
    var queryWrapper =
        JoinWrappers.lambda(ActivityPO.class)
            .selectAsClass(ActivityPO.class, ActivityDetailDTO.class)
            .selectAs(RepositoryPO::getId, ActivityDetailDTO::getRepositoryId)
            .selectAs(RepositoryPO::getRepositoryName, ActivityDetailDTO::getRepositoryName)
            .selectAs(UserPO::getUsername, ActivityDetailDTO::getUsername)
            .leftJoin(UserPO.class, UserPO::getId, ActivityPO::getUserId)
            .leftJoin(RepositoryPO.class, RepositoryPO::getId, ActivityPO::getRepositoryId)
            .eq(ActivityPO::getIsPullRequest, activityQueryDTO.isPullRequest());
    // issue的sub-issues可以来自不同仓库
    if(activityQueryDTO.parentId()!= null) {
      queryWrapper.eq(ActivityPO::getParentId, TypeConversionUtil.convertToLong(activityQueryDTO.parentId(),true));
    }
    else {
      queryWrapper.eq(RepositoryPO::getId, TypeConversionUtil.convertToLong(activityQueryDTO.repositoryId(),true));
    }
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
    if (activityQueryDTO.author() != null) {
      queryWrapper.eq(UserPO::getUsername, activityQueryDTO.author());
    }
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
    if (activityDetailDTOPage.getRecords().isEmpty()) return activityDetailDTOPage;
    // 提取ActivityPO的ID列表
    List<Long> activityIds =
        activityDetailDTOPage.getRecords().stream().map(ActivityDetailDTO::getId).toList();
    //  批量查询每个Activity的标签、指定的参与者和评论数
    Map<Long, List<LabelDTO>> labelMap =
        activityAssignLabelService.getLabelsByActivityIds(activityIds);
    Map<Long, List<AssigneeDTO>> assigneeMap =
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
  public ActivityDetailDTO getActivityDetailById(Long id) {
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
      List<LabelDTO> labelDTOList =
          activityAssignLabelService
              .getLabelsByActivityIds(List.of(id))
              .getOrDefault(id, Collections.emptyList());
      List<AssigneeDTO> assigneeDTOList =
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
      // 忽略返回值，因为允许删除时没有相关信息
      activityAssignLabelService.removeByActivityIds(activityIds);
      activityDesignateAssigneeService.removeByActivityIds(activityIds);
      commentService.removeByActivityIds(activityIds);
    }

    // 删除活动
    super.remove(new QueryWrapper<ActivityPO>().eq("repository_id", repositoryId));
  }

  @Override
  public Page<IssueDTO> pageSubIssue(Long parentId, Integer page, Integer size) {
    ActivityQueryDTO activityQueryDTO = new ActivityQueryDTO(parentId);

    Page<ActivityDetailDTO> activityDetailDTOPage = pageActivitiesDetail(
            activityQueryDTO, page, size);// 先获取活动列表，确保活动存在
    // 提取ActivityPO的ID列表
    List<Long> subIssueIds =
            activityDetailDTOPage.getRecords().stream().map(ActivityDetailDTO::getId).toList();
    if(subIssueIds.isEmpty()) {
      return new Page<>(page, size, 0);
    }
    // 查询每个issue的sub-issues数量
    var wrapper =
            JoinWrappers.lambda(ActivityPO.class)
                    .selectAs(ActivityPO::getParentId, IssueCountDTO::getIssueId)
                    .selectCount(ActivityPO::getParentId, IssueCountDTO::getCount)
                    .in(ActivityPO::getParentId, subIssueIds)
                    .groupBy(ActivityPO::getParentId);
    List<IssueCountDTO> issueCountDTOPage = activityMapper.selectJoinList(IssueCountDTO.class, wrapper);
    Map<Long,Long> issueCountMap =
            issueCountDTOPage.stream().collect(
                    java.util.stream.Collectors.toMap(IssueCountDTO::getIssueId, IssueCountDTO::getCount));

    // 查询每个issue已完成的sub-issues数量
    wrapper.isNotNull(ActivityPO::getGmtClosed);
    List<IssueCountDTO> closedIssueCountDTOPage = activityMapper.selectJoinList(IssueCountDTO.class, wrapper);
    Map<Long,Long> closedIssueCountMap =
            closedIssueCountDTOPage.stream().collect(
                    java.util.stream.Collectors.toMap(IssueCountDTO::getIssueId, IssueCountDTO::getCount));
    // 组装结果
    Page<IssueDTO> issueDTOPage = new Page<>(page, size, activityDetailDTOPage.getTotal());
    issueDTOPage.setRecords(
        activityDetailDTOPage.getRecords().stream()
            .map(
                activityDetailDTO ->
                    new IssueDTO(
                        activityDetailDTO.getId(),
                        activityDetailDTO.getNumber(),
                        activityDetailDTO.getRepositoryId(),
                        activityDetailDTO.getRepositoryName(),
                        activityDetailDTO.getTitle(),
                        activityDetailDTO.getDescription(),
                        activityDetailDTO.getUsername(),
                        activityDetailDTO.getAssignees(),
                        activityDetailDTO.getGmtClosed(),
                        issueCountMap.getOrDefault(activityDetailDTO.getId(), 0L),
                        closedIssueCountMap.getOrDefault(activityDetailDTO.getId(), 0L)))
            .toList());
    return issueDTOPage;
  }

  @Override
  public boolean removeSubIssueById(Long subIssueId) {
    LambdaUpdateWrapper<ActivityPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(ActivityPO::getId, subIssueId)
        .set(ActivityPO::getParentId, null);
    return super.update(updateWrapper);

  }
}
