package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import edu.cmipt.gcs.enumeration.CollaboratorOrderByEnum;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.pojo.user.UserPO;

public interface UserCollaborateRepositoryService extends IService<UserCollaborateRepositoryPO> {
  IPage<UserPO> pageCollaboratorsByRepositoryId(
      Long repositoryId, Page<UserPO> page, CollaboratorOrderByEnum orderBy, Boolean isAsc);
}
