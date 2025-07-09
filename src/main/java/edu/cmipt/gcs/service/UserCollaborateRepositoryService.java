package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.cmipt.gcs.enumeration.CollaboratorOrderByEnum;
import edu.cmipt.gcs.pojo.collaboration.CollaboratorDTO;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;

import java.util.List;

public interface UserCollaborateRepositoryService extends IService<UserCollaborateRepositoryPO> {
  Page<CollaboratorDTO> pageCollaboratorsByRepositoryId(
      Long repositoryId,
      Integer pageNum,
      Integer pageSize,
      CollaboratorOrderByEnum orderBy,
      Boolean isAsc);

  UserCollaborateRepositoryPO getOneByCollaboratorIdAndRepositoryId(
      Long collaboratorId, Long repositoryId);

  List<Long> removeByRepositoryId(Long repositoryId);
}
