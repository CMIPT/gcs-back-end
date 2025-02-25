# gcs-back-end

`git` 中央仓库服务的后端实现。

# 部署介绍

## 使用 `docker-compose` 进行部署

现在提供了 `docker-compose` 部署方式。

第一步，使用以下命令获取仓库：

```bash
git clone --recursive https://github.com/CMIPT/gcs-back-end.git
```

或者：

```bash
git clone https://github.com/CMIPT/gcs-back-end.git
git submodule init
git submodule update
```

确保安装 `mvn` 和 `jdk17`，使用 `mvn` 打包：

```bash
mvn package
```

配置 `.env` 中的环境变量，对于生产环境，你需要配置：

```bash
GIT_USER_PASSWORD=
POSTGRES_PASSWORD=
SPRING_DRUID_PASSWORD=
GIT_SERVER_DOMAIN=
SPRING_MAIL_HOST=
SPRING_MAIL_PORT=
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
SPRING_MAIL_PROTOCOL=
MD5_SALT=
GCS_SSH_MAPPING_PORT=
GCS_SPRING_MAPPING_PORT=
TARGET_JAR_PATH=
```

对于开发环境，你只需要配置：

```bash
SPRING_MAIL_HOST=
SPRING_MAIL_PORT=
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
SPRING_MAIL_PROTOCOL=
TARGET_JAR_PATH=
```

配置完成后，使用 `docker-compose build` 构建镜像，使用 `docker-compose up -d` 启动服务。

在开发过程中，可以通过以下命令对包进行替换：

```bash
mvn package && \
docker cp <jar_path> <container_id>:/gcs/gcs.jar && \
docker restart <container_id>
```
