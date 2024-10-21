package edu.cmipt.gcs.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class GitConstant {
    public static String GIT_USER_NAME;

    public static String GIT_HOME_DIRECTORY;

    public static String GIT_SERVER_DOMAIN;

    public static String GIT_SERVER_PORT;

    public static String GITOLITE_ADMIN_REPOSITORY_PATH;

    public static String GITOLITE_CONF_DIR_PATH;

    public static String GITOLITE_CONF_FILE_PATH;

    public static String GITOLITE_USER_CONF_DIR_PATH;

    public static String GITOLITE_REPOSITORY_CONF_DIR_PATH;

    public static String GITOLITE_KEY_DIR_PATH;

    @Value("${git.user.name}")
    public void setGIT_USER_NAME(String gitUserName) {
        GitConstant.GIT_USER_NAME = gitUserName;
    }

    @Value("${git.home.directory}")
    public void setGIT_HOME_DIRECTORY(String gitHomeDirectory) {
        GitConstant.GIT_HOME_DIRECTORY = gitHomeDirectory;
    }

    @Value("${git.server.domain}")
    public void setGIT_SERVER_DOMAIN(String gitServerDomain) {
        GitConstant.GIT_SERVER_DOMAIN = gitServerDomain;
    }

    @Value("${git.server.port}")
    public void setGIT_SERVER_PORT(String gitServerPort) {
        GitConstant.GIT_SERVER_PORT = gitServerPort;
    }

    @Value("${gitolite.admin.repository.path}")
    public void setGITOLITE_ADMIN_REPOSITORY_PATH(String gitoliteAdminRepositoryPath) {
        GitConstant.GITOLITE_ADMIN_REPOSITORY_PATH = gitoliteAdminRepositoryPath;
        GitConstant.GITOLITE_CONF_DIR_PATH =
                Paths.get(gitoliteAdminRepositoryPath, "conf").toString();
        GitConstant.GITOLITE_CONF_FILE_PATH =
                Paths.get(GITOLITE_CONF_DIR_PATH, "gitolite.conf").toString();
        GitConstant.GITOLITE_USER_CONF_DIR_PATH =
                Paths.get(GITOLITE_CONF_DIR_PATH, "gitolite.d", "user").toString();
        GitConstant.GITOLITE_REPOSITORY_CONF_DIR_PATH =
                Paths.get(GITOLITE_CONF_DIR_PATH, "gitolite.d", "repository").toString();
        GitConstant.GITOLITE_KEY_DIR_PATH =
                Paths.get(gitoliteAdminRepositoryPath, "keydir").toString();
    }
}
