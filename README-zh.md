# gcs-back-end
`git`中央仓库服务的后端实现。

# 部署设置说明
| 变量                         | 类型     | 默认值                                 | 说明 |
| -                            | -        | -                                      | - |
| `deploy`                     | `bool`   | `true`                                 | 是否进行部署，当为`false`只进行打包操作。 |
| `debug`                      | `bool`   | `false`                                | 是否启用调试模式。 |
| `skipTest`                   | `bool`   | `true`                                 | 是否跳过测试。 |
| `createGitUser`              | `bool`   | `true`                                 | 是否创建`git`用户。 |
| `deployWithDocker`           | `bool`   | `true`                                 | 是否使用`Docker`进行部署。 |
| `repositoryDirectory`        | `string` | `"/home/git/repositories"`             | `git`仓库存放目录。 |
| `postgresPassword`           | `string` | `null`                                 | `Postgres`数据库密码。 |
| `serviceEnable`              | `bool`   | `true`                                 | 是否启用`systemd`服务。 |
| `serviceName`                | `string` | `"gcs"`                                | `systemd`服务名称。 |
| `serviceSuffix`              | `string` | `"service"`                            | `systemd`服务文件后缀。 |
| `serviceDescription`         | `string` | `"Git server center back-end service"` | `systemd`服务描述。 |
| `servicePIDFile`             | `string` | `"/var/run/gcs.pid"`                   | `systemd`服务`PID`文件。 |
| `serviceUser`                | `string` | `gcs`                                  | `systemd`服务运行用户。 |
| `serviceUserPassword`        | `string` | `null`                                 | `systemd`服务运行用户密码。 |
| `serviceWorkingDirectory`    | `string` | `"/opt/gcs"`                           | `systemd`服务工作目录。 |
| `serviceRestartPolicy`       | `string` | `"always"`                             | `systemd`服务重启策略。 |
| `serviceRestartDelaySeconds` | `int`    | `5`                                    | `systemd`服务重启延迟时间。 |
| `serviceStartJavaCommand`    | `string` | `"/usr/bin/java"`                      | `systemd`服务启动`Java`命令。 |
| `serviceStartJavaArgs`       | `list`   | `["-jar"]`                             | `systemd`服务启动`Java`参数。 |
| `serviceStartJarFile`        | `string` | `"/opt/gcs/gcs.jar"`                   | `systemd`服务启动`Jar`文件。脚本会将`maven`打包出来的文件拷贝到该位置。 |
| `serviceAfter`               | `list`   | `["network.target"]`                   | `systemd`服务会在这些服务启动后启动。 |
| `serviceWantedBy`            | `list`   | `"multi-user.target"`                  | `systemd`服务会被这些服务依赖。 |

