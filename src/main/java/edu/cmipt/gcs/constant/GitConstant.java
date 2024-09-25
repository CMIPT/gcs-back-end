package edu.cmipt.gcs.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitConstant {
    public static String GIT_USER_NAME;

    public static String GIT_HOME_DIRECTORY;

    public static String GIT_REPOSITORY_DIRECTORY;

    public static String GIT_REPOSITORY_SUFFIX;

    public static String GIT_SERVER_DOMAIN;

    public static final String SSH_KEY_PREFIX =
            "no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty ";

    @Value("${git.user.name}")
    public void setGIT_USER_NAME(String gitUserName) {
        GitConstant.GIT_USER_NAME = gitUserName;
    }

    @Value("${git.home.directory}")
    public void setGIT_HOME_DIRECTORY(String gitHomeDirectory) {
        GitConstant.GIT_HOME_DIRECTORY = gitHomeDirectory;
    }

    @Value("${git.repository.directory}")
    public void setGIT_REPOSITORY_DIRECTORY(String gitRepositoryDirectory) {
        GitConstant.GIT_REPOSITORY_DIRECTORY = gitRepositoryDirectory;
    }

    @Value("${git.repository.suffix}")
    public void setGIT_REPOSITORY_SUFFIX(String gitRepositorySuffix) {
        GitConstant.GIT_REPOSITORY_SUFFIX = gitRepositorySuffix;
    }

    @Value("${git.server.domain}")
    public void setGIT_SERVER_DOMAIN(String gitServerDomain) {
        GitConstant.GIT_SERVER_DOMAIN = gitServerDomain;
    }
}
