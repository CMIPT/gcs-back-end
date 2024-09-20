package edu.cmipt.gcs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import edu.cmipt.gcs.dao.RepositoryMapper;
import edu.cmipt.gcs.dao.UserMapper;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, RepositoryPO>
implements RepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Value("${git.user.name}")
    private String gitUserName;
    @Value("${git.repository.directory}")
    private String gitRepositoryDirectory;
    @Value("${git.repository.suffix}")
    private String gitRepositorySuffix;
    @Value("${git.server.domain}")
    private String gitServerDomain;

    @Override
    public boolean save(RepositoryPO repositoryPO) {
        String repositorySavePath = Paths.get(gitRepositoryDirectory, userMapper.selectById(repositoryPO.getUserId()).getUsername(), repositoryPO.getRepositoryName() + gitRepositorySuffix).toString();
        logger.info("Repository save path: {}", repositorySavePath);
        try {
            ProcessBuilder repositoryInitializer = new ProcessBuilder("sudo", "-u", gitUserName, "git", "init", "--bare", repositorySavePath);
            Process process = repositoryInitializer.start();
            if (process.waitFor() != 0) {
                logger.error("Failed to initialize repository");
                process.errorReader().lines().forEach(logger::error);
                return false;
            }
            // TODO: add url in the repositoryPO
        } catch (Exception e) {
            logger.error("Failed to initialize repository: {}", e.getMessage());
            return false;
        }
        if (!super.save(repositoryPO)) { 
            try {
                ProcessBuilder dirRemover = new ProcessBuilder("sudo", "-u", gitUserName, "rm", "-rf", repositorySavePath);
                Process process = dirRemover.start();
                if(process.waitFor() != 0) {
                    logger.error("Failed to remove repository directory");
                    process.errorReader().lines().forEach(logger::error);
                }
            } catch (Exception e) {
                logger.error("Failed to remove repository directory: {}", e.getMessage());
            }
            return false;
        }
        return true;
    }
}
