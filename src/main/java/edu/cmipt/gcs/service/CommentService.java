package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<CommentPO> {
  void removeByActivityId(Serializable id);

  Map<Long, Long> getCommentCountByActivityIds(List<Long> activityIds);

  void removeByActivityIds(List<Long> activityIds);
}
