-- Creating Sequences

CREATE SEQUENCE public.projects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.repositories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Creating Tables

CREATE TABLE public.project (
    id integer NOT NULL DEFAULT nextval('public.projects_id_seq'::regclass),
    name character varying(50) NOT NULL,
    description text,
    repository_id integer NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    delete_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.repository (
    id integer NOT NULL DEFAULT nextval('public.repositories_id_seq'::regclass),
    name character varying(50) NOT NULL,
    description text,
    is_private boolean DEFAULT false,
    owner_id integer,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    delete_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.user_repository (
    user_id integer NOT NULL,
    repository_id integer NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    delete_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.user_table (
    id integer NOT NULL DEFAULT nextval('public.users_id_seq'::regclass),
    username character varying(50) NOT NULL,
    email character varying(50) NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    delete_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Adding Constraints

ALTER TABLE ONLY public.project
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.repository
    ADD CONSTRAINT repositories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_repository
    ADD CONSTRAINT user_repository_pkey PRIMARY KEY (user_id, repository_id);

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT users_username_key UNIQUE (username);

-- Adding Foreign Key Constraints

ALTER TABLE ONLY public.project
    ADD CONSTRAINT projects_repository_id_fkey FOREIGN KEY (repository_id) REFERENCES public.repository(id);

ALTER TABLE ONLY public.user_repository
    ADD CONSTRAINT user_repository_repository_id_fkey FOREIGN KEY (repository_id) REFERENCES public.repository(id);

ALTER TABLE ONLY public.user_repository
    ADD CONSTRAINT user_repository_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.user_table(id);

