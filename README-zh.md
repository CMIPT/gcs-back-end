# gcs-back-end
`git` 中央仓库服务的后端实现。

# 部署介绍
## 使用 `deploy_ubuntu.sh` 脚本进行部署
如果使用 `deploy_ubuntu.sh` 脚本进行自动部署，那么你只需要将仓库克隆到一个 `ubuntu` 的主机上，然后
使用 `root` 用户执行脚本即可实现部署。在部署的过程中会使用 `apt` 安装以下的包，你需要保证 `apt` 的
源能够获取到这些包：
* `postgresql`
* `postgresql-client`
* `openjdk-17-jdk-headless`
* `maven`
* `sudo`
* `git`
* `openssh-server`
* `systemd` (可选)

其中 `systemd` 是可选项，是否需要取决于 `serviceType` 的值是否是 `"systemd"`，当值为 `"systemd"` 的
时候需要保证 `systemd` 可以被 `apt` 安装。

自动部署是通过 `bash deploy_ubuntu.sh [config_file]` 来进行部署，当不指定 `config_file` 选项时将会
使用 `config.json` 作为默认值，所以推荐将配置直接写入到 `config.json` 中，然后通过
`bash deploy_ubuntu.sh` 进行部署。

在进行配置的时候，如果用户没有指定配置项，那么将会使用 `config_default.json` 中的值作为默认值，所以
请不要修改 `config_default.json` 文件中的内容。

通常情况下，用户配置的时候需要特别注意以下的配置选项：
* `gitUserpassword`：`git` 用户的密码，`git` 用户用于执行与 `git` 命令相关操作以及用户通过 `ssh`
协议获取仓库时将会使用 `git` 用户进行登录。
* `gitServerDomain`：`git` 服务器的域名，生成 `ssh` 链接时将会使用该域名。
* `gitServerPort`：`git` 服务器的端口，生成 `ssh` 链接时将会使用该端口。
* `deployWithDocker`：是否使用 `Docker` 进行部署，如果为 `true`，那么将会通过
`3rdparty/docker-script` 中的脚本自动创建 `docker` 并将程序部署在 `docker` 中。
* `dockerImage`：`Docker` 镜像，如果 `deployWithDocker` 为 `true`，那么将会使用该镜像。目前支持
`ubuntu`。
* `dockerPortMapping`：`Docker` 端口映射，如果 `deployWithDocker` 为 `true`，那么将会使用该端口映射。
* `serviceType`：部署的服务类型，如果 `deployWithDocker` 为真，那么该值只有为 `sys-init-v` 时脚本才
能正确执行，当然如果直接在 `docker` 内部执行自动部署脚本，那么该值可以为 `systemd` (`docker` 必须
使用 `--privileged` 选项进行创建)。
* `serviceUserPassword`：服务的用户密码，部署的服务 (`Java` 程序) 会以一个新的用户身份执行，该用户
的密码将会被设置为该值。
* `postgresUserPassword`：操作系统中的 `postgres` 用户的密码，`postgres` 用户会在安装 `postgresql`
的时候自动创建，部署脚本会将 `postgres` 用户的密码更改为该值。
* `postgresqlUserPassword`：`Postgres` 数据库用户的密码，部署脚本默认会创建一个名为 `gcs` 数据库
用户，该用户的密码将会被设置为该值。
* `postgresqlHost`：`Postgres` 数据库的主机地址，部署脚本会使用该地址进行数据库的连接。
* `postgresqlPort`：`Postgres` 数据库的端口，部署脚本会使用该端口进行数据库的连接。
* `druidLoginPassword`：`Druid` 登录密码。
* `md5Salt`：`MD5` 加密盐值，用于对用户密码进行加密。当完成部署后请不要修改此值，否则用户密码将无法
正确验证。推荐使用随机数生成的 `sha1` 等 `hash` 值。
* `frontEndUrl`: 前端地址，用于进行跨域配置。
* `staticPathPattern`：静态资源的匹配模式。
* `staticLocations`：静态资源的路径，需要使用绝对路径。

**注意**：需要注意的是，所有的后端接口均是以 `gcs` 开头，所以在静态资源路径下面不应该有名为 `gcs`
的文件或者文件夹。

**注意**：如果将前端直接部署在同一个域上面，那么可以设置 `frontEndUrl` 为空字符串，然后配置
`staticPathPattern` 和 `staticLocations` 为前端的静态资源路径。

下面列出了完整的配置选项：
| 变量                         | 类型     | 默认值                                 | 说明 |
| -                            | -        | -                                      | - |
| `deploy`                     | `bool`   | `true`                                 | 是否进行部署，当为 `false` 只进行打包操作。 |
| `profiles`                   | `list`   | `["dev"]`                              | 启动的配置类型。 |
| `deployLogLevel`             | `string` | `"info"`                               | 部署脚本的日志级别。 |
| `skipTest`                   | `bool`   | `true`                                 | 是否跳过测试。 |
| `gitUserName`                | `string` | `"git"`                                | 用于保存 `git` 仓库的用户名。 |
| `gitHomeDirectory`           | `string` | `"/home/git"`                          | `git` 用户的家目录。 |
| `gitUserPassword`            | `string` | `"git"`                                | 用于保存 `git` 仓库的用户密码。 |
| `gitRepositoryDirectory`     | `string` | `"/home/git/repository"`               | `git` 仓库存放目录。不要使用 `~`。 |
| `gitServerDomain`            | `string` | `"localhost"`                          | 服务器域名。 |
| `gitServerPort`              | `int`    | `22`                                   | 服务器端口。 |
| `gitRepositorySuffix`        | `string` | `".git"`                               | `git` 仓库后缀。 |
| `deployWithDocker`           | `bool`   | `true`                                 | 是否使用 `Docker` 进行部署。 |
| `dockerName`                 | `string` | `"gcs-backend"`                        | `Docker` 容器名称。 |
| `dockerImage`                | `string` | `"ubuntu:latest"`                      | `Docker` 镜像。 |
| `dockerPortMapping`          | `list`   | `["8080:8080"]`                        | `Docker` 端口映射。 |
| `dockerWithGpu`              | `bool`   | `false`                                | `Docker` 是否使用 `GPU`。 |
| `dockerSrcPath`              | `string` | `"/opt/gcs-back-end-src"`              | `Docker` 中源码路径。源码会被拷贝到该路径进行编译。 |
| `serviceType`                | `string` | `"systemd"`                            | 部署的服务类型，可选值为 `"systemd"` 和 `"sys-init-v"` |
| `serviceName`                | `string` | `"gcs"`                                | 服务名称。 |
| `serviceDescription`         | `string` | `"Git server center back-end service"` | 服务描述。 |
| `servicePIDFile`             | `string` | `"/var/run/gcs.pid"`                   | 服务 `PID` 文件。 |
| `serviceUser`                | `string` | `"gcs"`                                | 服务运行用户。 |
| `serviceUserPassword`        | `string` | `"gcs"`                                | 服务运行用户密码。 |
| `serviceStartJavaCommand`    | `string` | `"/usr/bin/java"`                      | 服务启动的 `Java` 命令。 |
| `serviceStartJavaArgs`       | `list`   | `["-jar"]`                             | 服务启动的 `Java` 参数。 |
| `serviceStartJarFile`        | `string` | `"/opt/gcs/gcs.jar"`                   | 服务启动的 `Jar` 文件。脚本会将 `maven` 打包出来的文件拷贝到该位置。 |
| `serviceEnable`              | `bool`   | `true`                                 | 是否启用 `systemd` 服务。 |
| `serviceSuffix`              | `string` | `".service"`                           | `systemd` 服务文件后缀。 |
| `serviceWorkingDirectory`    | `string` | `"/opt/gcs"`                           | `systemd` 服务工作目录。 |
| `serviceRestartPolicy`       | `string` | `"always"`                             | `systemd` 服务重启策略。 |
| `serviceRestartDelaySeconds` | `int`    | `5`                                    | `systemd` 服务重启延迟时间。 |
| `serviceAfter`               | `list`   | `["network.target"]`                   | `systemd` 服务会在这些服务启动后启动。 |
| `serviceWantedBy`            | `list`   | `["multi-user.target"]`                | `systemd` 服务会被这些服务依赖。 |
| `serviceSystemdDirectory`    | `string` | `"/etc/systemd/system"`                | `systemd` 服务文件存放目录。 |
| `serviceSysVInitDirectory`   | `string` | `"/etc/init.d"`                        | `Sys-Init-V` 服务文件存放目录。 |
| `serviceStartWithBoot`       | `bool`   | `true`                                 | `Sys-Init-V` 服务是否随系统启动。 |
| `serviceLogFile`             | `string` | `"/tmp/log/gcs.log"`                   | `Sys-Init-V` 服务日志文件。 |
| `postgresUserPassword`       | `string` | `"postgres"`                           | `Linux` 中 `Postgres` 用户密码。 |
| `postgresqlUserName`         | `string` | `"gcs"`                                | `Postgres` 用户名称。 |
| `postgresqlUserPassword`     | `string` | `"postgres"`                           | `Postgres` 用户密码。 |
| `postgresqlDatabaseName`     | `string` | `"gcs"`                                | `Postgres` 数据库名称。 |
| `postgresqlHost`             | `string` | `"localhost"`                          | `Postgres` 主机地址。 |
| `postgresqlPort`             | `int`    | `5432`                                 | `Postgres` 端口。 |
| `druidLoginUsername`         | `string` | `"druid"`                              | `Druid` 登录用户名。 |
| `druidLoginPassword`         | `string` | `"druid"`                              | `Druid` 登录密码。 |
| `deleteGitUser`              | `bool`   | `true`                                 | 清理时是否删除 `git` 用户。 |
| `deleteServiceUser`          | `bool`   | `true`                                 | 清理时是否删除 `service` 用户。 |
| `md5Salt`                    | `string` | `""`                                   | `MD5` 加密盐值。 |
| `frontEndUrl`                | `string` | `"http://localhost:3000"`              | 前端地址。 |
| `staticPathPattern`          | `string` | `null`                                 | 静态资源的匹配模式。 |
| `staticLocations`            | `list`   | `null`                                 | 静态资源的路径，使用绝对路径，例如 `['/home/gcs/static']`。 |

## 手动部署
手动部署可以在任意的 `UNIX-like` 系统上面进行，下面依次介绍你需要手动完成的操作。

### 前置环境安装
以下的软件包你必须进行安装：
* `postgresql`
* `postgresql-client`
* `openjdk-17-jdk-headless`
* `maven`
* `sudo`
* `git`
* `openssh-server`

### 初始化数据库
当你完成了数据库的配置后 (通常包括创建一个新的数据库和用户以及相关的授权工作)，你可以直接执行
`bash database/database_deploy.sh <db_user> <db_name> <db_host> <db_port> <db_password>` 来完成
数据库的自动初始化 (请确保工作目录为仓库的根目录)。

### `git` 用户创建
你需要创建一个用户用于执行仓库创建、克隆等操作，通常取名为 `git`，并且在其家目录下创建 `.ssh` 文件夹
并将权限修改为 `700`。

### 修改 `sudo` 配置
你需要保证你运行 `Java` 程序的用户能够在执行 `sudo -u <git_user> rm`，`sudo -u <git_user> tee` 以及
`sudo -u <git_user> git` 时不需要输入密码，其中 `<git_user>` 为 [git 用户创建](#git-用户创建) 时
创建的用户名。

通常你需要追加 `<java_user> ALL=(<git_user>) NOPASSWD：/usr/bin/git, /usr/bin/tee, /usr/bin/rm` 到
`/etc/sudoers` 文件中，其中 `<java_user>` 为你运行 `Java` 程序的用户。

### 配置 `application.yml` 或者 `application.properties`
你需要完成以下配置，这里以 `application.properties` 为例：

```properties
# 数据库用户名
spring.datasource.druid.username=
# 数据库用户密码
spring.datasource.druid.password=
# 数据库的主机地址
spring.datasource.druid.url=
# druid 的登录用户名
spring.datasource.druid.stat-view-servlet.login-username=
# druid 的登录密码
spring.datasource.druid.stat-view-servlet.login-password=
# 启动的配置文件，通常设置为 prod
spring.profiles.active=
# git 服务器的域名，通常为部署机器的公网 IP
git.server.domain=
# git 服务器的端口，设置为 ssh 连接时使用的端口
git.server.port=
# `git` 用户创建部分创建的用户名称
git.user.name=
# `git` 用户创建部分创建的家目录
git.home.directory=
# 仓库保存位置，需要确保 git.user.name 用户拥有 rwx 的权限
git.repository.directory=
# 仓库后缀，通常设置为 .git
git.repository.suffix=
# md5 的盐值，用于对用户密码进行加密
md5.salt=
# 前端地址，用于进行跨域配置
front-end.url=
# 静态资源的映射规则，/** 表示所有的静态资源都会被映射
spring.mvc.static-path-pattern=
# 静态资源的路径，需要使用绝对路径，例如 file:/static
spring.resources.static-locations=
```

**注意**：需要注意的是，所有的后端接口均是以 `gcs` 开头，所以在静态资源路径下面不应该有名为 `gcs`
的文件或者文件夹。

**注意**：如果将前端直接部署在同一个域上面，那么可以设置 `front-end.url` 为空字符串，然后配置
`spring.mvc.static-path-pattern` 和 `spring.resources.static-locations` 为前端的静态资源路径。

### 选择喜欢的方式进行部署
完成了上述操作后，此时已经可以通过 `mvn spring-boot:run` 直接启动了。之后你只需要选择自己喜欢的方式
进行部署即可，例如可以通过 `mvn package` 将项目打包成一个 `jar` 包然后部署成一个服务。

## 自动清理功能
当你使用 `deploy_ubuntu.sh` 进行自动部署时，你可以使用配套的 `clean_ubuntu.sh` 进行清理工作。

清理脚本的使用与部署脚本类似，你也需要指定配置文件位置，如果不指定则使用 `config.json` 作为默认值。
请确保清理脚本指定的配置文件和部署脚本指定的配置文件内容是一致的。

清理脚本的主要工作逻辑如下：

```bash
if deployWithDocker then
    stop docker
else
    stop service
    delete service files except the log file
    delete /etc/sudoers.d/{serviceUser}
    if deleteGitUser then
        delete git user but keep the home directory
    fi
    if deleteServiceUser then
        delete service user but keep the home directory
    fi
fi
```
