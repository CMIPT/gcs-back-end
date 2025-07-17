package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.CommentMapper;
import edu.cmipt.gcs.pojo.comment.CommentCountDTO;
import edu.cmipt.gcs.pojo.comment.CommentFullInfoDTO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.user.UserPO;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentPO>
    implements CommentService {

  private final CommentMapper commentMapper;

  public CommentServiceImpl(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  @Override
  public CommentPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean updateById(CommentPO comment) {
    return super.updateById(comment);
  }

  @Override
  public boolean save(CommentPO comment) {
    return super.save(comment);
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
  public Map<Long, Long> getCommentCountByActivityIds(List<Long> activityIds) {
    if (activityIds == null || activityIds.isEmpty()) {
      return Collections.emptyMap();
    }

    var wrapper =
        JoinWrappers.lambda(CommentPO.class)
            .selectAs(CommentPO::getActivityId, CommentCountDTO::getActivityId)
            .selectCount(CommentPO::getId, CommentCountDTO::getCount)
            .in(CommentPO::getActivityId, activityIds)
            .groupBy(CommentPO::getActivityId);
    List<CommentCountDTO> list = commentMapper.selectJoinList(CommentCountDTO.class, wrapper);

    return list.stream()
        .collect(Collectors.toMap(CommentCountDTO::getActivityId, CommentCountDTO::getCount));
  }

  @Override
  public List<Long> removeByActivityIds(List<Long> activityIds) {
    List<Long> commentIds =
        super.list(new QueryWrapper<CommentPO>().select("id").in("activity_id", activityIds))
            .stream()
            .map(CommentPO::getId)
            .collect(Collectors.toList());
    super.remove(new QueryWrapper<CommentPO>().in("activity_id", activityIds));
    return commentIds;
  }

  @Override
  public boolean updateHiddenState(Long commentId, Boolean isHidden) {
    CommentPO commentPO = super.getById(commentId);
    LambdaUpdateWrapper<CommentPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(CommentPO::getId, commentId);
    if (!isHidden) {
      updateWrapper.set(CommentPO::getGmtHidden, null);
      ;
    } else if (commentPO.getGmtHidden() == null) {
      updateWrapper.set(CommentPO::getGmtHidden, new Timestamp(System.currentTimeMillis()));
    } else {
      return true; // already hidden
    }
    return super.update(updateWrapper);
  }

  @Override
  public boolean updateResolvedState(Long commentId, Boolean isResolved) {
    CommentPO commentPO = super.getById(commentId);
    LambdaUpdateWrapper<CommentPO> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(CommentPO::getId, commentId);
    if (!isResolved) {
      updateWrapper.set(CommentPO::getGmtResolved, null);
      ;
    } else if (commentPO.getGmtResolved() == null) {
      updateWrapper.set(CommentPO::getGmtResolved, new Timestamp(System.currentTimeMillis()));
    } else {
      return true; // already resolved
    }
    return super.update(updateWrapper);
  }


  @Override
  public Page<CommentFullInfoDTO> pageCommentFullInfoByActivityId(Integer page, Integer size, Long activityId) {
    var wrapper = JoinWrappers.lambda(CommentPO.class)
        .selectAsClass(CommentPO.class, CommentFullInfoDTO.class)
        .selectAs(UserPO::getUsername, CommentFullInfoDTO::getUsername)
        .selectAs(UserPO::getEmail, CommentFullInfoDTO::getEmail)
        .selectAs(UserPO::getAvatarUrl, CommentFullInfoDTO::getAvatarUrl)
        .leftJoin(UserPO.class, UserPO::getId, CommentPO::getUserId)
        .eq(CommentPO::getActivityId, activityId)
        .isNull(CommentPO::getReplyToId) // 默认查询根评论,子评论通过另外的 API查询
        .orderBy(true, true, CommentPO::getGmtCreated);
    
    return commentMapper.selectJoinPage(new Page<>(page, size), CommentFullInfoDTO.class, wrapper);
  }

  @Override
  public Page<CommentFullInfoDTO> pageSubCommentFullInfoByActivityIdAndReplyToId(
      Integer page, Integer size, Long activityId, Long replyToId) {
    var wrapper = JoinWrappers.lambda(CommentPO.class)
        .selectAsClass(CommentPO.class, CommentFullInfoDTO.class)
        .selectAs(UserPO::getUsername, CommentFullInfoDTO::getUsername)
        .selectAs(UserPO::getEmail, CommentFullInfoDTO::getEmail)
        .selectAs(UserPO::getAvatarUrl, CommentFullInfoDTO::getAvatarUrl)
        .leftJoin(UserPO.class, UserPO::getId, CommentPO::getUserId)
        .eq(CommentPO::getActivityId, activityId)
        .eq(CommentPO::getReplyToId, replyToId) // 查询子评论
        .orderBy(true, true, CommentPO::getGmtCreated);
    
    return commentMapper.selectJoinPage(new Page<>(page, size), CommentFullInfoDTO.class, wrapper);
  }
}
