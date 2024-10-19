#!/usr/bin/bash

# Usage: bash prepare_dev.sh <db_postgres_password>

# install necessary packages
sudo apt-get update
sudo apt-get install -y postgresql postgresql-client openjdk-17-jdk-headless maven git openssh-server
if ! command -v redis-cli; then
    sudo apt-get install lsb-release curl gpg
    curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg
    sudo chmod 644 /usr/share/keyrings/redis-archive-keyring.gpg
    echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/redis.list
    sudo apt-get update
    sudo apt-get install redis
fi

# init the database
sudo su -c 'psql -c "DROP DATABASE IF EXISTS gcs_dev;"' postgres
sudo su -c 'psql -c "CREATE DATABASE gcs_dev;"' postgres
bash database/database_deploy.sh postgres gcs_dev localhost 5432 "$1"

# configure gitolite
sudo userdel -r git
sudo useradd -m git
sudo su -c 'git clone https://github.com/sitaramc/gitolite /home/git/gitolite' git
sudo su -c 'mkdir -p /home/git/bin; /home/git/gitolite/install -to /home/git/bin' git
sudo cp /home/"$USER"/.ssh/id_rsa.pub /home/git/"$USER".pub
sudo chown git:git /home/git/"$USER".pub
sudo su -c "/home/git/bin/gitolite setup -pk /home/git/$USER.pub" git
rm -rf /home/"$USER"/gitolite-admin
GIT_SSH_COMMAND='ssh -o StrictHostKeyChecking=no' git clone \
    ssh://git@localhost:22/gitolite-admin /home/"$USER"/gitolite-admin
mkdir -p /home/"$USER"/gitolite-admin/conf/gitolite.d
echo "
repo gitolite-admin
    RW+ = $USER
repo testing
    R = @all
include \"gitolite.d/*.conf\"
@all_public_repo =
repo @all_public_repo
    R = @all" > /home/"$USER"/gitolite-admin/conf/gitolite.conf
git -C /home/"$USER"/gitolite-admin add conf/gitolite.conf
git -C /home/"$USER"/gitolite-admin commit -m "Init the gitolite-admin"
git -C /home/"$USER"/gitolite-admin push

echo "$USER ALL=(git) NOPASSWD: /usr/bin/rm" | sudo tee /etc/sudoers.d/gcs_dev

echo "
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
spring.redis.host=localhost
spring.redis.port=6379
" > src/main/resources/application.properties
