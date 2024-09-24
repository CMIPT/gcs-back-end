package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.constant.GitConstant;
import edu.cmipt.gcs.dao.RepositoryMapper;
import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, RepositoryPO>
        implements RepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    @Autowired private UserMapper userMapper;

    /**
     * Save a repository and initialize a git repository in the file system.
     *
     * <p> Usually, the user will not create the same repository at the same time, so we don't
     * consider the thread competition
     */
    @Transactional
    @Override
    public boolean save(RepositoryPO repositoryPO) {
        if (!super.save(repositoryPO)) {
            logger.error("Failed to save repository to database");
            return false;
        }
        String repositorySavePath =
                Paths.get(
                                GitConstant.GIT_REPOSITORY_DIRECTORY,
                                userMapper.selectById(repositoryPO.getUserId()).getUsername(),
                                repositoryPO.getRepositoryName() + GitConstant.GIT_REPOSITORY_SUFFIX)
                        .toString();
        // check if the repositorySavePath has been created, if so, remove it
        // this may occur, if the last creation failed
        if (Files.exists(Paths.get(repositorySavePath))){
            logger.info("Repository save path exists, try to remove it");
            try {
                ProcessBuilder dirRemover =
                        new ProcessBuilder(
                                "sudo", "-u", GitConstant.GIT_USER_NAME, "rm", "-rf", repositorySavePath);
                Process process = dirRemover.start();
                if (process.waitFor() != 0) {
                    throw new GenericException(
                            ErrorCodeEnum.REPOSITORY_CREATE_FAILED,
                            process.errorReader().lines().toList().toString());
                }
            } catch (Exception e) {
                logger.error("Failed to remove repository directory: {}", e.getMessage());
                throw new GenericException(ErrorCodeEnum.REPOSITORY_CREATE_FAILED, e.getMessage());
            }
        }
        logger.info("Repository save path: {}", repositorySavePath);
        try {
            ProcessBuilder repositoryInitializer =
                    new ProcessBuilder(
                            "sudo", "-u", GitConstant.GIT_USER_NAME, "git", "init", "--bare", repositorySavePath);
            Process process = repositoryInitializer.start();
            if (process.waitFor() != 0) {
                throw new GenericException(
                        ErrorCodeEnum.REPOSITORY_CREATE_FAILED,
                        process.errorReader().lines().toList().toString());
            }
            // TODO: add url in the repositoryPO
        } catch (Exception e) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_CREATE_FAILED, e.getMessage());
        }
        return true;
    }
}
