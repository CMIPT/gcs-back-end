package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.GitConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class GitoliteUtil {
    private static final Logger logger = LoggerFactory.getLogger(GitoliteUtil.class);

    public static synchronized boolean initUserConfig(Long userId) {
        var userFileName = new StringBuilder().append(userId).append(".conf").toString();
        var userConfPath = Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName);
        if (Files.exists(userConfPath)) {
            logger.error("Duplicate user file");
            return false;
        }
        try {
            Files.createFile(userConfPath);
            String content =
                    """
                    @%d_public_repo = testing
                    @%d_private_repo = testing
                    @%d_ssh_key = @admin
                    repo @%d_private_repo
                        RW+ = @%d_ssh_key
                    repo @%d_public_repo
                        RW+ = @%d_ssh_key
                    """
                            .formatted(userId, userId, userId, userId, userId, userId, userId);
            Files.writeString(userConfPath, content);
            List<String> lines = Files.readAllLines(Paths.get(GitConstant.GITOLITE_CONF_FILE_PATH));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("@all_public_repo")) {
                    lines.set(i, line + ' ' + "@%d_public_repo".formatted(userId));
                    Files.write(Paths.get(GitConstant.GITOLITE_CONF_FILE_PATH), lines);
                    String message = "Add user " + userId;
                    Path[] files = {
                        Paths.get("conf", "gitolite.d", "user", userFileName),
                        Paths.get("conf", "gitolite.conf")
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push changes");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Failed during writing user configuration: ", e);
            return false;
        }
        logger.error("Can not find @all_public_repo in gitolite.conf");
        return false;
    }

    public static synchronized boolean addSshKey(Long sshKeyId, String key, Long userId) {
        var sshKeyFileName = new StringBuilder().append(sshKeyId).append(".pub").toString();
        var sshKeyPath = Paths.get(GitConstant.GITOLITE_KEY_DIR_PATH, sshKeyFileName);
        if (Files.exists(sshKeyPath)) {
            logger.error("Duplicate SSH file");
            return false;
        }
        try {
            var userFileName = new StringBuilder().append(userId).append(".conf").toString();
            Files.writeString(sshKeyPath, key);
            List<String> lines =
                    Files.readAllLines(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("@%d_ssh_key".formatted(userId))) {
                    lines.set(i, line + ' ' + sshKeyId);
                    Files.write(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName),
                            lines);
                    String message = "Add ssh key " + sshKeyId;
                    Path[] files = {
                        Paths.get("keydir", sshKeyFileName),
                        Paths.get("conf", "gitolite.d", "user", userFileName)
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error("Can not find @{}_ssh_key in user configuration".formatted(userId));
        return false;
    }

    public static synchronized boolean removeSshKey(Long sshKeyId, Long userId) {
        var sshKeyFileName = new StringBuilder().append(sshKeyId).append(".pub").toString();
        var sshKeyPath = Paths.get(GitConstant.GITOLITE_KEY_DIR_PATH, sshKeyFileName);
        if (!Files.exists(sshKeyPath)) {
            logger.warn("Trying to remove a non-existent SSH key file: {}", sshKeyPath);
            return true;
        }
        try {
            Files.delete(sshKeyPath);
        } catch (Exception e) {
            logger.error("Failed to delete SSH key file: {}", e.getMessage());
            return false;
        }
        try {
            var userFileName = new StringBuilder().append(userId).append(".conf").toString();
            List<String> lines =
                    Files.readAllLines(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("@%d_ssh_key".formatted(userId))) {
                    lines.set(i, line.replace(" " + sshKeyId, ""));
                    Files.write(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName),
                            lines);
                    String message = "Remove ssh key " + sshKeyId;
                    Path[] files = {
                        Paths.get("keydir", sshKeyFileName),
                        Paths.get("conf", "gitolite.d", "user", userFileName)
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error("Can not find @{}_ssh_key in user configuration".formatted(userId));
        return false;
    }

    public static synchronized boolean updateSshKey(Long sshKeyId, String key) {
        var sshKeyFileName = new StringBuilder().append(sshKeyId).append(".pub").toString();
        var sshKeyPath = Paths.get(GitConstant.GITOLITE_KEY_DIR_PATH, sshKeyFileName);
        if (!Files.exists(sshKeyPath)) {
            logger.error("Trying to update a non-existent SSH key file: {}", sshKeyPath);
            return false;
        }
        try {
            Files.writeString(sshKeyPath, key);
            String message = "Update ssh key " + sshKeyId;
            Path[] files = {Paths.get("keydir", sshKeyFileName)};
            if (!GitoliteUtil.commitAndPush(message, files)) {
                logger.error("Failed to commit and push");
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    public static synchronized boolean createRepository(
            Long repositoryId,
            String repositoryName,
            Long userId,
            String userName,
            boolean isPrivate) {
        var userFileName = new StringBuilder().append(userId).append(".conf").toString();
        var repositoryFileName =
                new StringBuilder().append(repositoryId).append(".conf").toString();
        var repositoryConfPath =
                Paths.get(GitConstant.GITOLITE_REPOSITORY_CONF_DIR_PATH, repositoryFileName);
        if (Files.exists(repositoryConfPath)) {
            logger.error("Duplicate repository file");
            return false;
        }
        try {
            Files.createFile(repositoryConfPath);
            String content =
                    """
                    @%d_repo_collaborator = @admin
                    repo %s/%s
                        RW+ = @%d_repo_collaborator
                    """
                            .formatted(repositoryId, userName, repositoryName, repositoryId);
            Files.writeString(repositoryConfPath, content);
            List<String> lines =
                    Files.readAllLines(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(
                        "@%d_%s_repo".formatted(userId, isPrivate ? "private" : "public"))) {
                    lines.set(i, line + ' ' + userName + '/' + repositoryName);
                    Files.write(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName),
                            lines);
                    String message = "Add repository " + userName + '/' + repositoryName;
                    Path[] files = {
                        Paths.get("conf", "gitolite.d", "user", userFileName),
                        Paths.get("conf", "gitolite.d", "repository", repositoryFileName)
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error(
                "Can not find @{}_{}_repo in user configuration"
                        .formatted(userId, isPrivate ? "private" : "public"));
        return false;
    }

    public static synchronized boolean removeRepository(
            String repositoryName, Long userId, String userName, boolean isPrivate) {
        var userFileName = new StringBuilder().append(userId).append(".conf").toString();
        try {
            List<String> lines =
                    Files.readAllLines(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(
                        "@%d_%s_repo".formatted(userId, isPrivate ? "private" : "public"))) {
                    lines.set(i, line.replace(" " + userName + '/' + repositoryName, ""));
                    Files.write(
                            Paths.get(GitConstant.GITOLITE_USER_CONF_DIR_PATH, userFileName),
                            lines);
                    String message = "Remove repository " + userName + '/' + repositoryName;
                    Path[] files = {Paths.get("conf", "gitolite.d", "repository", userFileName)};
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                    }
                    String repositorySavePath =
                            Paths.get(
                                            GitConstant.GIT_SERVER_HOME,
                                            "repositories",
                                            userName,
                                            repositoryName + ".git")
                                    .toString();
                    ProcessBuilder dirRemover =
                            new ProcessBuilder(
                                    "sudo",
                                    "-u",
                                    GitConstant.GIT_SERVER_USERNAME,
                                    "rm",
                                    "-rf",
                                    repositorySavePath);
                    Process process = dirRemover.start();
                    if (process.waitFor() != 0) {
                        logger.error("Failed to remove repository directory");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error(
                "Can not find @{}_{}_repo in user configuration"
                        .formatted(userId, isPrivate ? "private" : "public"));
        return false;
    }

    public static synchronized boolean addCollaborator(
            Long repositoryOwnerId, Long repositoryId, Long collaboratorId) {
        var repositoryFileName =
                new StringBuilder().append(repositoryId).append(".conf").toString();
        var repositoryConfPath =
                Paths.get(GitConstant.GITOLITE_REPOSITORY_CONF_DIR_PATH, repositoryFileName);
        if (!Files.exists(repositoryConfPath)) {
            logger.error("Repository file does not exist");
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(repositoryConfPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("@%d_repo_collaborator".formatted(repositoryId))) {
                    lines.set(i, line + " @%d_ssh_key".formatted(collaboratorId));
                    Files.write(repositoryConfPath, lines);
                    String message =
                            "Add collaborator " + collaboratorId + " to repository " + repositoryId;
                    Path[] files = {
                        Paths.get("conf", "gitolite.d", "repository", repositoryFileName)
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error(
                "Can not find @{}_repo_collaborator in repository configuration"
                        .formatted(repositoryId));
        return false;
    }

    public static synchronized boolean removeCollaborator(
            Long repositoryOwnerId, Long repositoryId, Long collaboratorId) {
        var repositoryFileName =
                new StringBuilder().append(repositoryId).append(".conf").toString();
        var repositoryConfPath =
                Paths.get(GitConstant.GITOLITE_REPOSITORY_CONF_DIR_PATH, repositoryFileName);
        if (!Files.exists(repositoryConfPath)) {
            logger.error("Repository file does not exist");
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(repositoryConfPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("@%d_repo_collaborator".formatted(repositoryId))) {
                    lines.set(i, line.replace(" @%d_ssh_key".formatted(collaboratorId), ""));
                    Files.write(repositoryConfPath, lines);
                    String message =
                            "Remove collaborator "
                                    + collaboratorId
                                    + " from repository "
                                    + repositoryId;
                    Path[] files = {
                        Paths.get("conf", "gitolite.d", "repository", repositoryFileName)
                    };
                    if (!GitoliteUtil.commitAndPush(message, files)) {
                        logger.error("Failed to commit and push");
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        logger.error(
                "Can not find @{}_repo_collaborator in repository configuration"
                        .formatted(repositoryId));
        return false;
    }

    private static synchronized boolean commitAndPush(String message, Path... files) {
        if (files.length == 0) {
            logger.error("No files to commit");
            return false;
        }
        try {
            List<String> command = new LinkedList<>();
            command.add("git");
            command.add("-C");
            command.add(GitConstant.GIT_SERVER_ADMIN_REPOSITORY);
            command.add("add");
            command.addAll(List.of(files).stream().map(Path::toString).toList());
            ProcessBuilder add = new ProcessBuilder(command);
            Process process = add.start();
            if (process.waitFor() != 0) {
                logger.error("Failed to add files: {}", List.of(files));
                throw new RuntimeException(process.errorReader().lines().toList().toString());
            }
            ProcessBuilder commit =
                    new ProcessBuilder(
                            "git",
                            "-C",
                            GitConstant.GIT_SERVER_ADMIN_REPOSITORY,
                            "commit",
                            "-m",
                            message);
            process = commit.start();
            if (process.waitFor() != 0) {
                logger.error("Failed to commit changes");
                throw new RuntimeException(process.errorReader().lines().toList().toString());
            }
            ProcessBuilder push =
                    new ProcessBuilder(
                            "git", "-C", GitConstant.GIT_SERVER_ADMIN_REPOSITORY, "push");
            process = push.start();
            if (process.waitFor() != 0) {
                logger.error("Failed to push changes");
                throw new RuntimeException(process.errorReader().lines().toList().toString());
            }
        } catch (Exception e) {
            // reset the state of the repository
            try {
                ProcessBuilder reset =
                        new ProcessBuilder(
                                "git",
                                "-C",
                                GitConstant.GIT_SERVER_ADMIN_REPOSITORY,
                                "reset",
                                "--hard",
                                "HEAD^");
                Process process = reset.start();
                if (process.waitFor() != 0) {
                    logger.error("Failed to reset repository");
                    throw new RuntimeException(process.errorReader().lines().toList().toString());
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }
}
