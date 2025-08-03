package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cmipt.gcs.dao.LabelMapper;
import edu.cmipt.gcs.enumeration.LabelOrderByEnum;
import edu.cmipt.gcs.pojo.label.LabelPO;
import java.io.Serializable;
import java.util.List;
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
  public List<Long> removeByRepositoryId(Long repositoryId) {
    List<Long> labelIds =
        super.list(new QueryWrapper<LabelPO>().select("id").eq("repository_id", repositoryId))
            .stream()
            .map(LabelPO::getId)
            .toList();
    super.remove(new QueryWrapper<LabelPO>().eq("repository_id", repositoryId));
    return labelIds;
  }

  @Override
  public Page<LabelPO> pageLabelsByRepositoryId(
      Long repositoryId,
      Boolean isAsc,
      LabelOrderByEnum orderBy,
      Integer pageNum,
      Integer pageSize) {
    Page<LabelPO> labelPage = new Page<>(pageNum, pageSize);
    var wrapper = new QueryWrapper<LabelPO>();
    wrapper.eq("repository_id", repositoryId);
    wrapper.orderBy(true, isAsc, orderBy.getFieldName());
    return super.page(labelPage, wrapper);
  }
}
