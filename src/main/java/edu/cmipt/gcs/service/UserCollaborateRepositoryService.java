package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import edu.cmipt.gcs.enumeration.CollaboratorOrderByEnum;
import edu.cmipt.gcs.pojo.collaboration.CollaboratorDTO;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;

public interface UserCollaborateRepositoryService extends IService<UserCollaborateRepositoryPO> {
  Page<CollaboratorDTO> pageCollaboratorsByRepositoryId(
      Long repositoryId, Page<CollaboratorDTO> page, CollaboratorOrderByEnum orderBy, Boolean isAsc);
}
