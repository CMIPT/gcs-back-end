package edu.cmipt.gcs.dao;

import com.github.yulichang.base.MPJBaseMapper;
import edu.cmipt.gcs.pojo.activity.ActivityPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ActivityMapper extends MPJBaseMapper<ActivityPO> {
  @Select(
      "SELECT * FROM t_activity WHERE repository_id = #{repositoryId} ORDER BY gmt_created DESC"
          + " LIMIT 1")
  ActivityPO getLatestActivityIgnoreLogicDeleted(@Param("repositoryId") Long repositoryId);
}
