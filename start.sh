#!/bin/bash

graceful_shutdown() {
    if [ -n "$java_pid" ]; then
        kill -TERM "$java_pid" 2>/dev/null
        wait "$java_pid"
    fi
    exit 0
}
trap graceful_shutdown SIGTERM SIGINT

cd "$JAVA_WORKING_DIRECTORY" || exit 1
chown -R "$GIT_USER_NAME:$GIT_USER_MAIN_GROUP" "$GIT_USER_HOME/repositories" || exit 1
if [ -f .output/gcs-back-end.jar ]; then
    mv .output/gcs-back-end.jar . || exit 1
fi

cd "$GITOLITE_ADMIN_REPOSITORY" || exit 1
service ssh restart || exit 1
git fetch || exit 1
git reset --hard origin/master || exit 1
cp ~/.ssh/id_rsa.pub keydir/root.pub || exit 1
git commit -am 'Update root.pub'
git push -f

cd "$JAVA_WORKING_DIRECTORY" || exit 1
if [ -f .output/server/index.mjs ]; then
    java -jar gcs-back-end.jar &
    java_pid=$!
    exec node .output/server/index.mjs
else
    exec java -jar gcs-back-end.jar
fi
