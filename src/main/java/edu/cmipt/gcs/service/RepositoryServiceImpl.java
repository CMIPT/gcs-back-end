package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cmipt.gcs.dao.RepositoryMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.util.GitoliteUtil;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, RepositoryPO>
    implements RepositoryService {
  private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

  @Autowired private UserService userService;

  @Override
  public RepositoryPO getById(Serializable id) {
    return super.getById(id);
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
    RepositoryPO repositoryPO = super.getById(id);
    assert repositoryPO != null;
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
