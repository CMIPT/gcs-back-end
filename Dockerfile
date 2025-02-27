FROM ubuntu:latest

RUN apt-get update && apt-get install -y sudo openssh-server git openjdk-17-jre-headless nodejs

ARG GIT_USER_NAME=git
ARG GIT_USER_MAIN_GROUP="$GIT_USER_NAME"
ARG GIT_USER_PASSWORD="$GIT_USER_NAME"
ARG GIT_USER_HOME="/home/$GIT_USER_NAME"
ARG GITOLITE_REPOSITORY="$GIT_USER_HOME/gitolite"
ARG GITOLITE_INSTALLATION_DIR="$GIT_USER_HOME/bin"
ARG GITOLITE_ADMIN_REPOSITORY=/root/gitolite-admin
ARG GITOLITE_ADMIN_REPOSITORY_USER_NAME=root
ARG GITOLITE_ADMIN_REPOSITORY_USER_EMAIL=root@localhost
ARG GITOLITE_PATH=./3rdparty/gitolite
ARG JAVA_WORKING_DIRECTORY=/gcs
ARG TARGET_JAR_PATH=./target/gcs-back-end.jar

RUN useradd -m "$GIT_USER_NAME" && echo "$GIT_USER_NAME:$GIT_USER_PASSWORD" | chpasswd

COPY "$GITOLITE_PATH" "$GITOLITE_REPOSITORY"

RUN chown -R "$GIT_USER_NAME:$GIT_USER_MAIN_GROUP" "$GITOLITE_REPOSITORY" && \
    sudo -u "$GIT_USER_NAME" mkdir -p "$GITOLITE_INSTALLATION_DIR" && \
    sudo -u "$GIT_USER_NAME" "$GITOLITE_REPOSITORY/install" -to "$GITOLITE_INSTALLATION_DIR" && \
    ssh-keygen -t rsa -b 4096 -f /root/.ssh/id_rsa -N "" && \
    cp /root/.ssh/id_rsa.pub "$GIT_USER_HOME/root.pub" && \
    chown "$GIT_USER_NAME:$GIT_USER_MAIN_GROUP" "$GIT_USER_HOME/root.pub" && \
    sudo -u "$GIT_USER_NAME" "$GITOLITE_INSTALLATION_DIR/gitolite" setup -pk "$GIT_USER_HOME/root.pub"

RUN service ssh restart && \
    ssh-keyscan -p 22 localhost >> /root/.ssh/known_hosts && \
    git clone "$GIT_USER_NAME@localhost:gitolite-admin" "$GITOLITE_ADMIN_REPOSITORY" && \
    mkdir -p "$GITOLITE_ADMIN_REPOSITORY/conf/gitolite.d/user" && \
    mkdir -p "$GITOLITE_ADMIN_REPOSITORY/conf/gitolite.d/repository" && \
    echo "\
@admin = root\n\
repo gitolite-admin\n\
    RW+ = @admin\n\
repo testing\n\
    RW+ = @admin\n\
include \"gitolite.d/user/*.conf\"\n\
include \"gitolite.d/repository/*.conf\"\n\
@all_public_repo = testing\n\
repo @all_public_repo\n\
    R = @all" > "$GITOLITE_ADMIN_REPOSITORY/conf/gitolite.conf" && \
    git -C "$GITOLITE_ADMIN_REPOSITORY" config user.name "$GITOLITE_ADMIN_REPOSITORY_USER_NAME" && \
    git -C "$GITOLITE_ADMIN_REPOSITORY" config user.email "$GITOLITE_ADMIN_REPOSITORY_USER_EMAIL" && \
    git -C "$GITOLITE_ADMIN_REPOSITORY" commit -am "Init the gitolite-admin" && \
    git -C "$GITOLITE_ADMIN_REPOSITORY" push

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 22 8080

WORKDIR "$JAVA_WORKING_DIRECTORY"

RUN mkdir -p .output
COPY "$TARGET_JAR_PATH" .output* .output

RUN echo "\
    if [ -f $JAVA_WORKING_DIRECTORY/.output/gcs-back-end.jar ]; then mv $JAVA_WORKING_DIRECTORY/.output/gcs-back-end.jar $JAVA_WORKING_DIRECTORY/gcs-back-end.jar; fi && \
    service ssh restart && \
    git -C $GITOLITE_ADMIN_REPOSITORY fetch && \
    git -C $GITOLITE_ADMIN_REPOSITORY reset --hard origin/master && \
    cp ~/.ssh/id_rsa.pub $GITOLITE_ADMIN_REPOSITORY/keydir/root.pub && \
    (git -C $GITOLITE_ADMIN_REPOSITORY commit -am 'Update root.pub' && git push -f || true) && \
    if [ -f $JAVA_WORKING_DIRECTORY/.output/server/index.mjs ]; then node /gcs/.output/server/index.mjs & fi && \
    java -jar gcs-back-end.jar" \
    > \
    "start.sh"

ENTRYPOINT ["bash", "start.sh"]

