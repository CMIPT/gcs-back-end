-- The constarint of the primary key and unique key is added to the table.
ALTER TABLE ONLY public.t_repository
    ADD CONSTRAINT pk_repository PRIMARY KEY (id);
CREATE UNIQUE INDEX unique_t_repository_name_user_id ON public.t_repository
    (LOWER(repository_name), user_id, gmt_deleted);

-- The constraint of t_user is added to the table.
ALTER TABLE ONLY public.t_user ADD CONSTRAINT pk_user_table PRIMARY KEY (id);
CREATE UNIQUE INDEX unique_t_user_username ON public.t_user (LOWER(username), gmt_deleted);
CREATE UNIQUE INDEX unique_t_user_email ON public.t_user (LOWER(email), gmt_deleted);

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
    ADD CONSTRAINT unique_t_ssh_key_user_id_public_key
    UNIQUE (user_id, public_key, gmt_deleted);
ALTER TABLE ONLY public.t_ssh_key
    ADD CONSTRAINT unique_t_ssh_key_user_id_name_key
    UNIQUE (user_id, name, gmt_deleted);

-- The constraint of t_user_collaborate_repository is added to the table.
ALTER TABLE ONLY public.t_user_collaborate_repository
    ADD CONSTRAINT pk_user_collaborate_repository PRIMARY KEY (id);
ALTER TABLE ONLY public.t_user_collaborate_repository
    ADD CONSTRAINT t_user_collaborate_repository_collaborator_id_repository_id
    UNIQUE (collaborator_id, repository_id, gmt_deleted);

-- The constraint of t_activity_designate_assignee is added to the table.
ALTER TABLE ONLY public.t_activity_designate_assignee
    ADD CONSTRAINT pk_activity_designate_assignee PRIMARY KEY (id);
ALTER TABLE ONLY public.t_activity_designate_assignee
    ADD CONSTRAINT t_activity_designate_assignee_activity_id_assignee_id
    UNIQUE (activity_id, assignee_id, gmt_deleted);

-- The constraint of t_activity_assign_label is added to the table.
ALTER TABLE ONLY public.t_activity_assign_label
    ADD CONSTRAINT pk_activity_assign_label PRIMARY KEY (id);
ALTER TABLE ONLY public.t_activity_assign_label
    ADD CONSTRAINT t_activity_assign_label_user_id_activity_id_label_id
    UNIQUE (user_id,activity_id, label_id, gmt_deleted);

-- The constraint of t_activity is added to the table.
ALTER TABLE ONLY public.t_activity
    ADD CONSTRAINT pk_activity PRIMARY KEY (id);
ALTER TABLE ONLY public.t_activity
    ADD CONSTRAINT t_activity_number_repository_id
    UNIQUE (number, repository_id, gmt_deleted);
CREATE INDEX idx_repo_activity ON public.t_activity(repository_id, number DESC);


-- The constraint of t_activity_comment is added to the table.
ALTER TABLE ONLY public.t_activity_comment
    ADD CONSTRAINT pk_activity_comment PRIMARY KEY (id);

-- The constraint of t_label added to the table.
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT pk_label PRIMARY KEY (id);
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT unique_t_label_name_repository_id
    UNIQUE (name, repository_id, gmt_deleted);
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT unique_t_label_color_repository_id
    UNIQUE (hex_color, repository_id, gmt_deleted);
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT ck_label_color
    CHECK (hex_color ~ '^#[0-9A-Fa-f]{6}$');
