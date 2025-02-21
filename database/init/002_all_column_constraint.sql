-- The constarint of the primary key and unique key is added to the table.
ALTER TABLE ONLY public.t_repository
ADD CONSTRAINT pk_repository PRIMARY KEY (id);
ALTER TABLE ONLY public.t_repository
ADD CONSTRAINT unique_t_repository_name_user_id
UNIQUE (repository_name, user_id, gmt_deleted);

-- The constraint of t_user is added to the table.
ALTER TABLE ONLY public.t_user
ADD CONSTRAINT pk_user_table PRIMARY KEY (id);
ALTER TABLE ONLY public.t_user
ADD CONSTRAINT unique_t_user_username
UNIQUE (username, gmt_deleted);
ALTER TABLE ONLY public.t_user
ADD CONSTRAINT unique_t_user_email
UNIQUE (email, gmt_deleted);

-- The constraint of t_user_star_repository is added to the table.
ALTER TABLE ONLY public.t_user_star_repository
ADD CONSTRAINT pk_user_star_repository PRIMARY KEY (id);
ALTER TABLE ONLY public.t_user_star_repository
ADD CONSTRAINT unique_t_user_star_repository_user_id_repository_id
UNIQUE (user_id, repository_id, gmt_deleted);

-- The constraint of t_ssh_key is added to the table.
ALTER TABLE ONLY public.t_ssh_key
ADD CONSTRAINT pk_ssh_key PRIMARY KEY (id);
ALTER TABLE ONLY public.t_ssh_key
ADD CONSTRAINT unique_t_ssh_key_user_id_name_key
UNIQUE (user_id, public_key, gmt_deleted);
UNIQUE (user_id, name, gmt_deleted);

-- The constraint of t_user_collaborate_repository is added to the table.
ALTER TABLE ONLY public.t_user_collaborate_repository
ADD CONSTRAINT pk_user_collaborate_repository PRIMARY KEY (id);
ALTER TABLE ONLY public.t_user_collaborate_repository
ADD CONSTRAINT t_user_collaborate_repository_collaborator_id_repository_id
UNIQUE (collaborator_id, repository_id, gmt_deleted);
