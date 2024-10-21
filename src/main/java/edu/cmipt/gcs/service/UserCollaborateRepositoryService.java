package edu.cmipt.gcs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.pojo.user.UserPO;

public interface UserCollaborateRepositoryService extends IService<UserCollaborateRepositoryPO> {
    List<UserPO> listCollaboratorsByRepositoryId(Long repositoryId, Page<UserPO> page);
}
