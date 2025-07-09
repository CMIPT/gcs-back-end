package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.activity.ActivityDetailDTO;
import edu.cmipt.gcs.pojo.activity.ActivityPO;
import edu.cmipt.gcs.pojo.activity.ActivityQueryDTO;
import edu.cmipt.gcs.pojo.issue.IssueDTO;

public interface ActivityService extends IService<ActivityPO> {

  ActivityPO getLatestActivityByRepositoryId(Long repositoryId);

  Page<ActivityDetailDTO> pageActivitiesDetail(
      ActivityQueryDTO activityQueryDTO, Integer pageNum,Integer pageSize);

  ActivityDetailDTO getActivityDetailById(Long id);

  ActivityPO getOneByActivityNumberAndRepositoryId(Long activityNumber, Long repositoryId);

  void removeByRepositoryId(Long repositoryId);

  Page<IssueDTO> pageSubIssue(Long parentId, Integer page, Integer size);

  boolean removeSubIssueById(Long subIssueId);
}
