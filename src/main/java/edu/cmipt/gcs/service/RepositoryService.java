package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;

public interface RepositoryService extends IService<RepositoryPO> {
  RepositoryPO getOneByUserIdAndRepositoryName(Long userId, String repositoryName);
}
