package edu.cmipt.gcs.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class GitConstant {
    public static String GIT_SERVER_USERNAME;

    public static String GIT_SERVER_HOME;

    public static String GIT_SERVER_DOMAIN;

    public static String GIT_SERVER_PORT;

    public static String GIT_SERVER_ADMIN_REPOSITORY;

    public static String GITOLITE_CONF_DIR_PATH;

    public static String GITOLITE_CONF_FILE_PATH;

    public static String GITOLITE_USER_CONF_DIR_PATH;

    public static String GITOLITE_REPOSITORY_CONF_DIR_PATH;

    public static String GITOLITE_KEY_DIR_PATH;

    @Value("${git.server.username}")
    public void setGIT_SERVER_USERNAME(String gitServerUserName) {
        GitConstant.GIT_SERVER_USERNAME = gitServerUserName;
    }

    @Value("${git.server.home}")
    public void setGIT_SERVER_HOME(String gitServerHome) {
        GitConstant.GIT_SERVER_HOME = gitServerHome;
    }

    @Value("${git.server.domain}")
    public void setGIT_SERVER_DOMAIN(String gitServerDomain) {
        GitConstant.GIT_SERVER_DOMAIN = gitServerDomain;
    }

    @Value("${git.server.port}")
    public void setGIT_SERVER_PORT(String gitServerPort) {
        GitConstant.GIT_SERVER_PORT = gitServerPort;
    }

    @Value("${git.server.admin.repository}")
    public void setGIT_SERVER_ADMIN_REPOSITORY(String gitoliteAdminRepositoryPath) {
        GitConstant.GIT_SERVER_ADMIN_REPOSITORY = gitoliteAdminRepositoryPath;
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
