package edu.cmipt.gcs.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.UserCollaborateRepositoryMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.util.GitoliteUtil;
import edu.cmipt.gcs.pojo.user.UserPO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserCollaborateRepositoryServiceImpl extends ServiceImpl<UserCollaborateRepositoryMapper, UserCollaborateRepositoryPO>
implements UserCollaborateRepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(UserCollaborateRepositoryServiceImpl.class);
    
    @Autowired RepositoryService repositoryService;
    @Autowired UserService userService;

    @Override
    @Transactional
    public boolean save(UserCollaborateRepositoryPO userCollaborateRepository) {
        if (!super.save(userCollaborateRepository)) {
            logger.error("Failed to save user collaborate repository to database");
            return false;
        }
        Long repositoryId = userCollaborateRepository.getRepositoryId();
        Long collaboratorId = userCollaborateRepository.getCollaboratorId();
        Long repositoryUserId = repositoryService.getById(repositoryId).getUserId();
        if (!GitoliteUtil.addCollaborator(repositoryUserId, repositoryId, collaboratorId)) {
            logger.error("Failed to add collaborator to gitolite");
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_ADD_FAILED, collaboratorId, repositoryId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        var userCollaborateRepository = super.getById(id);
        Long repositoryId = userCollaborateRepository.getRepositoryId();
        Long collaboratorId = userCollaborateRepository.getCollaboratorId();
        Long repositoryUserId = repositoryService.getById(repositoryId).getUserId();
        if (!super.removeById(id)) {
            logger.error("Failed to remove user collaborate repository from database");
            return false;
        }
        if (!GitoliteUtil.removeCollaborator(repositoryUserId, repositoryId, collaboratorId)) {
            logger.error("Failed to remove collaborator from gitolite");
            throw new GenericException(
                    ErrorCodeEnum.COLLABORATION_REMOVE_FAILED, collaboratorId, repositoryId);
        }
        return true;
    }

    @Override
    public List<UserPO> listCollaboratorsByRepositoryId(Long repositoryId, Page<UserPO> page) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        List<Long> collaboratorIds = super.listObjs(new QueryWrapper<UserCollaborateRepositoryPO>()
                .eq("repository_id", repositoryId)
                .select("collaborator_id"));
        if (collaboratorIds == null || collaboratorIds.isEmpty()) {
            return List.of();
        }
        queryWrapper.in("id", collaboratorIds);
        return userService.list(page, queryWrapper);
    }
}
