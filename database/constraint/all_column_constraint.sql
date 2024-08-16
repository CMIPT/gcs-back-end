-- The constarint of the primary key and unique key is added to the table.
ALTER TABLE ONLY public.t_repository 
ADD CONSTRAINT pk_repository PRIMARY KEY (pk_repository_id);

-- The constraint of t_user is added to the table.
ALTER TABLE ONLY public.t_user 
ADD CONSTRAINT pk_user_table PRIMARY KEY (pk_user_id);

ALTER TABLE ONLY public.t_user 
ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY public.t_user 
ADD CONSTRAINT users_username_key UNIQUE (username);

-- The constraint of t_user_repository is not necessary, 
-- as the primary key is already unique.
ALTER TABLE ONLY public.t_user_repository 
ADD CONSTRAINT pk_user_repository PRIMARY KEY (pk_user_repository_id);
