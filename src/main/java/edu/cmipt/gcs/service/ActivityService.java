package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.activity.ActivityFullInfoDTO;
import edu.cmipt.gcs.pojo.activity.ActivityPO;
import edu.cmipt.gcs.pojo.activity.ActivityQueryDTO;
import edu.cmipt.gcs.pojo.issue.IssueDTO;

import java.util.List;

public interface ActivityService extends IService<ActivityPO> {

  ActivityPO getLatestActivityByRepositoryId(Long repositoryId);

  Page<ActivityFullInfoDTO> pageActivityFullInfo(
      ActivityQueryDTO activityQueryDTO, Integer pageNum,Integer pageSize);

  ActivityFullInfoDTO getActivityFullInfoById(Long id);

  ActivityPO getOneByActivityNumberAndRepositoryId(Long activityNumber, Long repositoryId);

  List<Long> removeByRepositoryId(Long repositoryId);

  Page<IssueDTO> pageSubIssueByParentId(Long parentId, Integer page, Integer size);

  boolean removeSubIssueById(Long subIssueId);

  boolean updateLockedState(Long activityId, boolean isLocked);

  boolean updateClosedState(Long activityId, Boolean isClosed);
}
