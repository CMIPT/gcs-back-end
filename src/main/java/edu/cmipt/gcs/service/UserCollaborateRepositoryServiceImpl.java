package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import edu.cmipt.gcs.dao.UserCollaborateRepositoryMapper;
import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.enumeration.CollaboratorOrderByEnum;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.collaboration.CollaboratorDTO;
import edu.cmipt.gcs.pojo.collaboration.UserCollaborateRepositoryPO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.util.GitoliteUtil;
import edu.cmipt.gcs.util.RedisUtil;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCollaborateRepositoryServiceImpl
    extends ServiceImpl<UserCollaborateRepositoryMapper, UserCollaborateRepositoryPO>
    implements UserCollaborateRepositoryService {
  private static final Logger logger =
      LoggerFactory.getLogger(UserCollaborateRepositoryServiceImpl.class);

  @Autowired private RedisTemplate<String, Object> redisTemplate;
  @Autowired RepositoryService repositoryService;
  @Autowired UserMapper userMapper;

  @Override
  public UserCollaborateRepositoryPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public UserCollaborateRepositoryPO getOneByCollaboratorIdAndRepositoryId(
      Long collaboratorId, Long repositoryId) {
    return super.getOne(
        new QueryWrapper<UserCollaborateRepositoryPO>()
            .eq("collaborator_id", collaboratorId)
            .eq("repository_id", repositoryId));
  }

  @Override
  public void removeByRepositoryId(Long repositoryId) {
    super.remove(new QueryWrapper<UserCollaborateRepositoryPO>().eq("repository_id", repositoryId));
  }

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
    var userCollaborateRepository =
        (UserCollaborateRepositoryPO)
            redisTemplate.opsForValue().get(RedisUtil.generateKey(this, id.toString()));
    if (userCollaborateRepository == null) {
      userCollaborateRepository = super.getById(id);
    }
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
  public Page<CollaboratorDTO> pageCollaboratorsByRepositoryId(
      Long repositoryId,
      Integer pageNum,
      Integer pageSize,
      CollaboratorOrderByEnum orderBy,
      Boolean isAsc) {
    Page<CollaboratorDTO> page = new Page<>(pageNum, pageSize);
    var queryWrapper =
        JoinWrappers.lambda(UserPO.class)
            .selectAsClass(UserCollaborateRepositoryPO.class, CollaboratorDTO.class)
            .selectAs(UserPO::getUsername, CollaboratorDTO::getUsername)
            .selectAs(UserPO::getEmail, CollaboratorDTO::getEmail)
            .selectAs(UserPO::getAvatarUrl, CollaboratorDTO::getAvatarUrl)
            .innerJoin(
                UserCollaborateRepositoryPO.class,
                UserCollaborateRepositoryPO::getCollaboratorId,
                UserPO::getId)
            .eq(UserCollaborateRepositoryPO::getRepositoryId, repositoryId);
    switch (orderBy) {
      case USERNAME:
        queryWrapper.orderBy(true, isAsc, UserPO::getUsername);
        break;
      case EMAIL:
        queryWrapper.orderBy(true, isAsc, UserPO::getEmail);
        break;
      case GMT_CREATED:
        queryWrapper.orderBy(true, isAsc, UserCollaborateRepositoryPO::getGmtCreated);
        break;
    }
    return userMapper.selectJoinPage(page, CollaboratorDTO.class, queryWrapper);
  }
}
