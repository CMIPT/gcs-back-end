package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cmipt.gcs.dao.RepositoryMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.util.GitoliteUtil;
import edu.cmipt.gcs.util.RedisUtil;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, RepositoryPO>
    implements RepositoryService {
  private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

  private final UserService userService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ActivityService activityService;
  private final LabelService labelService;
  private final UserCollaborateRepositoryService userCollaborateRepositoryService;

  public RepositoryServiceImpl(
      @Lazy UserCollaborateRepositoryService userCollaborateRepositoryService,
      LabelService labelService,
      ActivityService activityService,
      UserService userService,
      RedisTemplate<String, Object> redisTemplate) {
    this.userCollaborateRepositoryService = userCollaborateRepositoryService;
    this.labelService = labelService;
    this.activityService = activityService;
    this.userService = userService;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public RepositoryPO getById(Serializable id) {
    return super.getById(id);
  }

  @Override
  public boolean updateById(RepositoryPO repository) {
    return super.updateById(repository);
  }

  @Override
  public RepositoryPO getOneByUserIdAndRepositoryName(Long userId, String repositoryName) {
    return super.getOne(
        new QueryWrapper<RepositoryPO>()
            .eq("user_id", userId)
            .apply("LOWER(repository_name) = LOWER({0})", repositoryName));
  }

  /**
   * Save a repository and initialize a git repository in the file system.
   *
   * <p>Usually, the user will not create the same repository at the same time, so we don't consider
   * the thread competition
   */
  @Transactional
  @Override
  public boolean save(RepositoryPO repositoryPO) {
    if (!super.save(repositoryPO)) {
      logger.error("Failed to save repository to database");
      return false;
    }
    if (!GitoliteUtil.createRepository(
        repositoryPO.getId(),
        repositoryPO.getRepositoryName(),
        repositoryPO.getUserId(),
        userService.getById(repositoryPO.getUserId()).getUsername(),
        repositoryPO.getIsPrivate())) {
      logger.error("Failed to create repository in gitolite");
      throw new GenericException(ErrorCodeEnum.REPOSITORY_CREATE_FAILED, repositoryPO);
    }
    return true;
  }

  @Override
  @Transactional
  public boolean removeById(Serializable id) {
    var repositoryPO =
        (RepositoryPO) redisTemplate.opsForValue().get(RedisUtil.generateKey(this, id.toString()));
    if (repositoryPO == null) {
      repositoryPO = super.getById(id);
    }
    // 递归删除该仓库的所有活动、标签、合作者
    // 这里忽略返回值，由CacheAspect处理缓存删除
    activityService.removeByRepositoryId(repositoryPO.getId());
    labelService.removeByRepositoryId(repositoryPO.getId());
    userCollaborateRepositoryService.removeByRepositoryId(repositoryPO.getId());

    if (!super.removeById(id)) {
      logger.error("Failed to remove repository from database");
      return false;
    }
    if (!GitoliteUtil.removeRepository(
        repositoryPO.getRepositoryName(),
        repositoryPO.getUserId(),
        userService.getById(repositoryPO.getUserId()).getUsername(),
        repositoryPO.getIsPrivate())) {
      logger.error("Failed to remove repository from gitolite");
      throw new GenericException(ErrorCodeEnum.REPOSITORY_DELETE_FAILED, repositoryPO);
    }
    return true;
  }
}
