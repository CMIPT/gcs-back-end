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

自动部署是通过 `bash deploy_ubuntu.sh [config_file]` 来进行部署 (你需要以 `root` 用户身份运行该命令)，
当不指定 `config_file` 选项时将会使用 `config.json` 作为默认值，所以推荐将配置直接写入到
`config.json` 中，然后通过 `bash deploy_ubuntu.sh` 进行部署。

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
使用 `--privileged` 选项进行创建)，如果部署主机使用 `systemd` 进行服务管理，那么该值应为 `systemd`；
如果使用 `Sys-Init-V` 进行服务管理，那么该值应为 `sys-init-v`。
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

**注意**：如果启用 `deployWithDocker` 你需要确保 `Docker` 已经安装并且 `Docker` 守护进程已经启动。
同时你需要拉取子仓库，你可以通过 `git submodule update --init --recursive` 来拉取子仓库。

下面列出了完整的配置选项：
| 变量                         | 类型     | 默认值                                 | 说明 |
| -                            | -        | -                                      | - |
| `profiles`                   | `list`   | `["dev"]`                              | 启动的配置类型。 |
| `deployLogLevel`             | `string` | `"info"`                               | 部署脚本的日志级别。 |
| `adminName`                  | `string` | `"gcs-admin"`                          | `gcsolite` 提交时的用户名。 |
| `adminEmail`                 | `string` | `"gcs-admin@localhost"`                | `gcsolite` 提交时的邮箱。 |
| `gitUserName`                | `string` | `"git"`                                | 用于保存 `git` 仓库的用户名。 |
| `gitHomeDirectory`           | `string` | `"/home/git"`                          | `git` 用户的家目录。 |
| `gitUserPassword`            | `string` | `"git"`                                | 用于保存 `git` 仓库的用户密码。 |
| `gitServerDomain`            | `string` | `"localhost"`                          | 服务器域名。 |
| `gitServerPort`              | `int`    | `22`                                   | 服务器端口。 |
| `localSshdPort`              | `int`    | `22`                                   | 本地 `sshd` 端口。 |
| `deployWithDocker`           | `bool`   | `false`                                | 是否使用 `Docker` 进行部署。 |
| `dockerName`                 | `string` | `"gcs-backend"`                        | `Docker` 容器名称。 |
| `dockerImage`                | `string` | `"ubuntu:latest"`                      | `Docker` 镜像。 |
| `dockerPortMapping`          | `list`   | `["8080:8080"]`                        | `Docker` 端口映射。 |
| `dockerWithGpu`              | `bool`   | `false`                                | `Docker` 是否使用 `GPU`。 |
| `dockerSrcPath`              | `string` | `"/opt/gcs-back-end-src"`              | `Docker` 中源码路径。源码会被拷贝到该路径进行编译。 |
| `serviceType`                | `string` | `"sys-init-v"`                         | 部署的服务类型，可选值为 `"systemd"` 和 `"sys-init-v"` |
| `serviceName`                | `string` | `"gcs"`                                | 服务名称。 |
| `serviceDescription`         | `string` | `"Git server center back-end service"` | 服务描述。 |
| `servicePIDFile`             | `string` | `"/var/run/gcs.pid"`                   | 服务 `PID` 文件。 |
| `serviceUser`                | `string` | `"gcs"`                                | 服务运行用户。 |
| `serviceUserPassword`        | `string` | `"gcs"`                                | 服务运行用户密码。 |
| `serviceUserHomeDirectory`   | `string` | `"/home/gcs"`                          | 服务运行用户家目录。 |
| `serviceStartJavaCommand`    | `string` | `"/usr/bin/java"`                      | 服务启动的 `Java` 命令。 |
| `serviceStartJavaArgs`       | `list`   | `["-jar"]`                             | 服务启动的 `Java` 参数。 |
| `serviceStartJarFile`        | `string` | `"/opt/gcs/gcs.jar"`                   | 服务启动的 `Jar` 文件。脚本会将 `maven` 打包出来的文件拷贝到该位置。 |
| `serviceStartWithBoot`       | `bool`   | `true`                                 | 服务是否随系统启动。 |
| `serviceSuffix`              | `string` | `".service"`                           | `systemd` 服务文件后缀。 |
| `serviceWorkingDirectory`    | `string` | `"/opt/gcs"`                           | `systemd` 服务工作目录。 |
| `serviceRestartPolicy`       | `string` | `"always"`                             | `systemd` 服务重启策略。 |
| `serviceRestartDelaySeconds` | `int`    | `5`                                    | `systemd` 服务重启延迟时间。 |
| `serviceAfter`               | `list`   | `["network.target"]`                   | `systemd` 服务会在这些服务启动后启动。 |
| `serviceWantedBy`            | `list`   | `["multi-user.target"]`                | `systemd` 服务会被这些服务依赖。 |
| `serviceSystemdDirectory`    | `string` | `"/etc/systemd/system"`                | `systemd` 服务文件存放目录。 |
| `serviceSysVInitDirectory`   | `string` | `"/etc/init.d"`                        | `Sys-Init-V` 服务文件存放目录。 |
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
你需要创建一个用户用于执行仓库创建、克隆等操作，通常取名为 `git`，如果你需要重新配置 `gitolite`
你需要确保该用户是新创建的而不是已有的。

### `gitolite` 初始化
首先你需要确定最后你的 `Java` 程序会以哪个用户的身份运行，假设你已经确定最后你会以 `gcs` 用户的身份
运行。按照以下步骤进行 `gitolite` 的初始化：

1. 克隆 `gitolite` 的仓库到 `git` 用户 (取决于你在 [创建 `git` 用户](#git-用户创建) 步骤中创建的用户)
的家目录。你可以通过 `su -c 'git clone https://github.com/sitaramc/gitolite /home/git/gitolite' git`
命令来实现 (可能需要输入 `git` 用户密码)。
2. 通过以下命令安装 `gitolite`：

```bash
# 可能需要输入 `git` 用户密码
su -c 'mkdir -p /home/git/bin' git
su -c '/home/git/gitolite/install -to /home/git/bin' git
```

3. 拷贝 `gcs` 用户的公钥到 `git` 用户的家目录下面，通常为 `/home/git`，并且重命名为 `gcs.pub`：

```bash
# 可能 `root` 权限
su -c 'cp /home/gcs/.ssh/id_rsa.pub /home/git/gcs.pub' git
chown git:git /home/git/gcs.pub
```

4. 设置 `gitolite` 的管理员：

```bash
# 可能需要输入 `git` 用户密码
su -c '/home/git/bin/gitolite setup -pk /home/git/gcs.pub' git
```

5. 克隆 `gitolite-admin` 仓库到 `gcs` 用户的家目录下面：

```bash
# 可能需要输入 `gcs` 用户密码
# 你需要注意 `sshd` 的端口是否为 `22`
su -c 'git clone ssh://git@localhost:22/gitolite-admin /home/gcs/gitolite-admin' gcs
```

6. 初始化 `gitolite-admin` 仓库。为了保证程序的正确运行，你需要用下面代码块中的内容替换
`conf/gitolite.conf` 文件中的内容，替换后你需要提交并推送：
`git commit -am "Init gcs commit"` 和 `git push`。这个过程中可能会提示你对 `gcs` 用户 配置提交的
用户名和邮箱，你可以按照提示进行配置。

```bash
repo gitolite-admin
    RW+ = {config.serviceUser}
repo testing
    R = @all
include "gitolite.d/*.conf"
@all_public_repo =
repo @all_public_repo
    R = @all
```

### 修改 `sudo` 配置
你需要保证你运行 `Java` 程序的用户能够在执行 `sudo -u <git_user> rm`
时不需要输入密码，其中 `<git_user>` 为 [git 用户创建](#git-用户创建) 时创建的用户名。

通常你需要追加 `<java_user> ALL=(<git_user>) NOPASSWD: /usr/bin/rm` 到
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
# md5 的盐值，用于对用户密码进行加密
md5.salt=
# 前端地址，用于进行跨域配置
front-end.url=
# 静态资源的映射规则，/** 表示所有的静态资源都会被映射
spring.mvc.static-path-pattern=
# 静态资源的路径，需要使用绝对路径，例如 file:/static
spring.resources.static-locations=
# gitolite 仓库所在的路径
gitolite.admin.repository.path=
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

## 开发者工具
对于开发者而言，使用自动部署脚本并不方便，开发者需要的是一个能够自动部署好所需要环境的脚本，而不需要
部署整个服务。

为了方便开发者的开发，在仓库的根目录下面提供了 `prepare_dev.sh` 脚本，该脚本会自动完成以下工作：
* 安装 `postgresql`，`postgresql-client`，`openjdk-17-jdk-headless`，`maven`，`git`，`openssh-server`。
* 删除名为 `gcs_dev` 的数据库并以 `postgres` 用户创建一个新的 `gcs_dev` 数据库以及相关的表。
* 删除名为 `git` 的用户并创建一个新的 `git` 用户。为 `git` 用户安装 `gitolite`。
* 删除 `home/"$USER"/gitolite-admin` 目录并重新克隆 `gitolite-admin` 仓库到
`home/"$USER"/gitolite-admin` (其中的 `$USER` 将会被替换成脚本的执行者，后续同理)。
* 初始化 `gitolite-admin` 仓库的相关配置。
* 覆盖写 `$USER ALL=(git) NOPASSWD: /usr/bin/rm` 到 `/etc/sudoers.d/gcs_dev` 文件。
* 覆盖写 `application.properties`。

`application.properties` 中的内容将会被覆盖为：

```properties
spring.datasource.druid.username=postgres
spring.datasource.druid.password=$1
spring.datasource.druid.url=jdbc:postgresql://localhost:5432/gcs_dev
spring.datasource.druid.stat-view-servlet.login-username=druid
spring.datasource.druid.stat-view-servlet.login-password=druid
spring.profiles.active=dev
git.server.domain=localhost
git.server.port=22
git.user.name=git
git.home.directory=/home/git
md5.salt=Is that the best you can do?
front-end.url=
spring.mvc.static-path-pattern=
spring.resources.static-locations=
gitolite.admin.repository.path=/home/$USER/gitolite-admin
```

其中 `$1` 会被替换成执行脚本时传入的第一个参数。脚本的使用方法为
`bash prepare_dev.sh <db_postgres_password>`，其中的 `<db_postgres_password>` 为
`postgres` 用户 (这里指数据库中的用户而不是 `OS` 中的用户) 的密码，在执行脚本之前确保当前用户可以使用
`sudo` 进行操作。

在脚本执行成功后，开发者便可以通过 `mvn spring-boot:run` 启动程序，或者通过 `mvn test` 执行单元测试。

**注意**：有时在执行 `mvn spring-boot:run` 或者 `mvn test` 时会提示 `target` 目录的权限不够，这往往
是因为在使用 `bash prepare_dev.sh <db_postgres_password` 之前可能以其他用户的身份执行过 `mvn` 命令，
导致 `target` 的所有者不是当前用户，这时候你只需要删除 `target` 目录即可。
