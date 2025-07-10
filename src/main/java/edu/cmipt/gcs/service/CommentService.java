package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<CommentPO> {
  List<Long> removeByActivityId(Serializable id);

  Map<Long, Long> getCommentCountByActivityIds(List<Long> activityIds);

  List<Long> removeByActivityIds(List<Long> activityIds);

  boolean updateHiddenState(Long commentId, Boolean isHidden);

  boolean updateResolvedState(Long commentId, Boolean isResolved);

  Page<CommentPO> pageCommentByActivityId(Integer page, Integer size, Long activityId);

  Page<CommentPO> pageSubCommentByActivityIdAndReplyToId(
      Integer page, Integer size, Long activityId, Long replyToId);
}
