package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cmipt.gcs.dao.LabelMapper;
import edu.cmipt.gcs.pojo.label.LabelPO;
import java.io.Serializable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabelServiceImpl extends ServiceImpl<LabelMapper, LabelPO> implements LabelService {
  private final ActivityAssignLabelService activityAssignLabelService;

  public LabelServiceImpl(ActivityAssignLabelService activityAssignLabelService) {
    this.activityAssignLabelService = activityAssignLabelService;
  }

  @Override
  public LabelPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean save(LabelPO labelPO) {
    return super.save(labelPO);
  }

  @Override
  public boolean updateById(LabelPO labelPO) {
    return super.updateById(labelPO);
  }

  @Override
  @Transactional
  public boolean removeById(Serializable id) {
    // 将标签从所有活动中移除
    activityAssignLabelService.removeByLabelId(id);
    return super.removeById(id);
  }

  @Override
  public LabelPO getOneByNameAndRepositoryId(String labelName, Long repositoryId) {
    return super.getOne(
        new QueryWrapper<LabelPO>()
            .eq("repository_id", repositoryId)
            .apply("LOWER(name) = LOWER({0})", labelName));
  }


  @Override
  public void removeByRepositoryId(Long repositoryId) {
    super.remove(new QueryWrapper<LabelPO>().eq("repository_id", repositoryId));
  }
}
