package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.enumeration.LabelOrderByEnum;
import edu.cmipt.gcs.pojo.label.LabelPO;
import java.util.List;

public interface LabelService extends IService<LabelPO> {
  LabelPO getOneByNameAndRepositoryId(String labelName, Long repositoryId);

  List<Long> removeByRepositoryId(Long repositoryId);

  Page<LabelPO> pageLabelsByRepositoryId(
      Long repositoryId,
      Boolean isAsc,
      LabelOrderByEnum orderBy,
      Integer pageNum,
      Integer pageSize);
}
