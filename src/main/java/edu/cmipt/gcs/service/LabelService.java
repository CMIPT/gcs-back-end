package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.label.LabelPO;
import java.util.List;

public interface LabelService extends IService<LabelPO> {
  LabelPO getOneByNameAndRepositoryId(String labelName, Long repositoryId);

  List<Long> removeByRepositoryId(Long repositoryId);
}
