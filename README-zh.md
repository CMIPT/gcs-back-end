# gcs-back-end
`git` 中央仓库服务的后端实现。

# 部署设置说明
| 变量                         | 类型     | 默认值                                 | 说明 |
| -                            | -        | -                                      | - |
| `deploy`                     | `bool`   | `true`                                 | 是否进行部署，当为 `false` 只进行打包操作。 |
| `profiles`                   | `list`   | `["dev"]`                              | 启动的配置类型。 |
| `deployLogLevel`             | `string` | `"info"`                               | 部署脚本的日志级别。 |
| `skipTest`                   | `bool`   | `true`                                 | 是否跳过测试。 |
| `createGitUser`              | `bool`   | `true`                                 | 是否创建 `git` 用户。 |
| `deployWithDocker`           | `bool`   | `true`                                 | 是否使用 `Docker` 进行部署。 |
| `dockerName`                 | `string` | `"gcs-backend"`                        | `Docker` 容器名称。 |
| `dockerImage`                | `string` | `"ubuntu:latest"`                      | `Docker` 镜像。 |
| `dockerPortMapping`          | `list`   | `["8080:8080"]`                        | `Docker` 端口映射。 |
| `dockerWithGpu`              | `bool`   | `false`                                | `Docker` 是否使用 `GPU`。 |
| `dockerSrcPath`              | `string` | `"/opt/gcs-back-end-src"`              | `Docker` 中源码路径。源码会被拷贝到该路径进行编译。 |
| `repositoryDirectory`        | `string` | `"/home/git/repositories"`             | `git` 仓库存放目录。 |
| `serviceEnable`              | `bool`   | `true`                                 | 是否启用 `systemd` 服务。 |
| `serviceName`                | `string` | `"gcs"`                                | 服务名称。 |
| `serviceDescription`         | `string` | `"Git server center back-end service"` | 服务描述。 |
| `servicePIDFile`             | `string` | `"/var/run/gcs.pid"`                   | 服务 `PID` 文件。 |
| `serviceUser`                | `string` | `"gcs"`                                | 服务运行用户。 |
| `serviceUserPassword`        | `string` | `"gcs"`                                | 服务运行用户密码。 |
| `serviceStartJavaCommand`    | `string` | `"/usr/bin/java"`                      | 服务启动的 `Java` 命令。 |
| `serviceStartJavaArgs`       | `list`   | `["-jar"]`                             | 服务启动的 `Java` 参数。 |
| `serviceStartJarFile`        | `string` | `"/opt/gcs/gcs.jar"`                   | 服务启动的 `Jar` 文件。脚本会将 `maven` 打包出来的文件拷贝到该位置。 |
| `serviceSuffix`              | `string` | `"service"`                            | `systemd` 服务文件后缀。 |
| `serviceWorkingDirectory`    | `string` | `"/opt/gcs"`                           | `systemd` 服务工作目录。 |
| `serviceRestartPolicy`       | `string` | `"always"`                             | `systemd` 服务重启策略。 |
| `serviceRestartDelaySeconds` | `int`    | `5`                                    | `systemd` 服务重启延迟时间。 |
| `serviceAfter`               | `list`   | `["network.target"]`                   | `systemd` 服务会在这些服务启动后启动。 |
| `serviceWantedBy`            | `list`   | `"multi-user.target"`                  | `systemd` 服务会被这些服务依赖。 |
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

