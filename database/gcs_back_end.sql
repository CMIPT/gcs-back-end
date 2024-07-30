--
-- PostgreSQL database dump
--
-- Dumped from database version 15.7
-- Dumped by pg_dump version 16.1
SET 
  statement_timeout = 0;
SET 
  lock_timeout = 0;
SET 
  idle_in_transaction_session_timeout = 0;
SET 
  client_encoding = 'UTF8';
SET 
  standard_conforming_strings = on;
SELECT 
  pg_catalog.set_config('search_path', '', false);
SET 
  check_function_bodies = false;
SET 
  xmloption = content;
SET 
  client_min_messages = warning;
SET 
  row_security = off;
--
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: -
--
CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;
--
-- Name: EXTENSION adminpack; Type: COMMENT; Schema: -; Owner: 
--
COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';
--
-- Name: update_gmt_updated_column(); Type: FUNCTION; Schema: public; Owner: postgres
--
CREATE FUNCTION public.update_gmt_updated_column() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN NEW.gmt_updated = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$;
ALTER FUNCTION public.update_gmt_updated_column() OWNER TO postgres;
--
-- Name: issues_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.issues_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.issues_id_seq OWNER TO postgres;
--
-- Name: repositories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.repositories_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.repositories_id_seq OWNER TO postgres;
--
-- Name: repository_issue_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.repository_issue_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.repository_issue_id_seq OWNER TO postgres;
SET 
  default_tablespace = '';
SET 
  default_table_access_method = heap;
--
-- Name: t_repository; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.t_repository (
  pk_repository_id bigint DEFAULT nextval(
    'public.repositories_id_seq' :: regclass
  ) NOT NULL, 
  repository_name character varying(255) NOT NULL, 
  repository_description text DEFAULT '' :: text NOT NULL, 
  is_private boolean DEFAULT false, 
  user_id bigint NOT NULL, 
  star integer DEFAULT 0 NOT NULL, 
  fork integer DEFAULT 0 NOT NULL, 
  watcher integer DEFAULT 0 NOT NULL, 
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_deleted timestamp without time zone, 
  CONSTRAINT repository_fork_check CHECK (
    (fork >= 0)
  ), 
  CONSTRAINT repository_pk_repository_id_check CHECK (
    (pk_repository_id >= 0)
  ), 
  CONSTRAINT repository_star_check CHECK (
    (star >= 0)
  ), 
  CONSTRAINT repository_user_id_check CHECK (
    (user_id >= 0)
  ), 
  CONSTRAINT repository_watcher_check CHECK (
    (watcher >= 0)
  )
);
ALTER TABLE 
  public.t_repository OWNER TO postgres;
--
-- Name: TABLE t_repository; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON TABLE public.t_repository IS 'Table for storing repository information.';
--
-- Name: COLUMN t_repository.pk_repository_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.pk_repository_id IS 'Primary key of the repository table.';
--
-- Name: COLUMN t_repository.repository_name; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.repository_name IS 'Name of the repository.';
--
-- Name: COLUMN t_repository.repository_description; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.repository_description IS 'Description of the repository.';
--
-- Name: COLUMN t_repository.is_private; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.is_private IS 'Indicates if the repository is private.';
--
-- Name: COLUMN t_repository.user_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.user_id IS 'ID of the user who owns the repository.';
--
-- Name: COLUMN t_repository.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.gmt_created IS 'Timestamp when the repository was created.';
--
-- Name: COLUMN t_repository.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.gmt_updated IS 'Timestamp when the repository was last updated.';
--
-- Name: COLUMN t_repository.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_repository.gmt_deleted IS 'Timestamp when the repository was deleted.';
--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.users_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.users_id_seq OWNER TO postgres;
--
-- Name: t_user; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.t_user (
  pk_user_id bigint DEFAULT nextval(
    'public.users_id_seq' :: regclass
  ) NOT NULL, 
  username character varying(50) NOT NULL, 
  email character varying(254) NOT NULL, 
  user_password character(128) NOT NULL, 
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_deleted timestamp without time zone, 
  CONSTRAINT user_table_pk_user_id_check CHECK (
    (pk_user_id >= 0)
  )
);
ALTER TABLE 
  public.t_user OWNER TO postgres;
--
-- Name: TABLE t_user; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON TABLE public.t_user IS 'Table for storing user information.';
--
-- Name: COLUMN t_user.pk_user_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.pk_user_id IS 'Primary key of the user table.';
--
-- Name: COLUMN t_user.username; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.username IS 'Username of the user.';
--
-- Name: COLUMN t_user.email; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.email IS 'Email address of the user.';
--
-- Name: COLUMN t_user.user_password; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.user_password IS 'Password of the user, stored as an MD5 hash.';
--
-- Name: COLUMN t_user.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.gmt_created IS 'Timestamp when the user record was created.';
--
-- Name: COLUMN t_user.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.gmt_updated IS 'Timestamp when the user record was last updated.';
--
-- Name: COLUMN t_user.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user.gmt_deleted IS 'Timestamp when the user record was deleted.';
--
-- Name: user_repository_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.user_repository_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.user_repository_id_seq OWNER TO postgres;
--
-- Name: t_user_repository; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.t_user_repository (
  pk_user_repository_id bigint DEFAULT nextval(
    'public.user_repository_id_seq' :: regclass
  ) NOT NULL, 
  user_id bigint NOT NULL, 
  repository_id bigint NOT NULL, 
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_deleted timestamp without time zone, 
  CONSTRAINT user_repository_pk_user_repository_id_check CHECK (
    (pk_user_repository_id >= 0)
  ), 
  CONSTRAINT user_repository_repository_id_check CHECK (
    (repository_id >= 0)
  ), 
  CONSTRAINT user_repository_user_id_check CHECK (
    (user_id >= 0)
  )
);
ALTER TABLE 
  public.t_user_repository OWNER TO postgres;
--
-- Name: TABLE t_user_repository; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON TABLE public.t_user_repository IS 'Table for storing relationships between users and repositories.';
--
-- Name: COLUMN t_user_repository.pk_user_repository_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.pk_user_repository_id IS 'Primary key of the user_repository table.';
--
-- Name: COLUMN t_user_repository.user_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.user_id IS 'ID of the user.';
--
-- Name: COLUMN t_user_repository.repository_id; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.repository_id IS 'ID of the repository.';
--
-- Name: COLUMN t_user_repository.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.gmt_created IS 'Timestamp when the relationship was created.';
--
-- Name: COLUMN t_user_repository.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.gmt_updated IS 'Timestamp when the relationship was last updated.';
--
-- Name: COLUMN t_user_repository.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--
COMMENT ON COLUMN public.t_user_repository.gmt_deleted IS 'Timestamp when the relationship was deleted.';
--
-- Data for Name: t_repository; Type: TABLE DATA; Schema: public; Owner: postgres
--
COPY public.t_repository (
  pk_repository_id, repository_name, 
  repository_description, is_private, 
  user_id, star, fork, watcher, gmt_created, 
  gmt_updated, gmt_deleted
) 
FROM 
  stdin;
\.--
-- Data for Name: t_user; Type: TABLE DATA; Schema: public; Owner: postgres
--
COPY public.t_user (
  pk_user_id, username, email, user_password, 
  gmt_created, gmt_updated, gmt_deleted
) 
FROM 
  stdin;
\.--
-- Data for Name: t_user_repository; Type: TABLE DATA; Schema: public; Owner: postgres
--
COPY public.t_user_repository (
  pk_user_repository_id, user_id, repository_id, 
  gmt_created, gmt_updated, gmt_deleted
) 
FROM 
  stdin;
\.--
-- Name: issues_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--
SELECT 
  pg_catalog.setval('public.issues_id_seq', 1, false);
--
-- Name: repositories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--
SELECT 
  pg_catalog.setval(
    'public.repositories_id_seq', 1, 
    false
  );
--
-- Name: repository_issue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--
SELECT 
  pg_catalog.setval(
    'public.repository_issue_id_seq', 
    1, false
  );
--
-- Name: user_repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--
SELECT 
  pg_catalog.setval(
    'public.user_repository_id_seq', 
    1, false
  );
--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--
SELECT 
  pg_catalog.setval('public.users_id_seq', 1, false);
--
-- Name: t_repository pk_repository; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE 
  ONLY public.t_repository 
ADD 
  CONSTRAINT pk_repository PRIMARY KEY (pk_repository_id);
--
-- Name: t_user_repository pk_user_repository; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE 
  ONLY public.t_user_repository 
ADD 
  CONSTRAINT pk_user_repository PRIMARY KEY (pk_user_repository_id);
--
-- Name: t_user pk_user_table; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE 
  ONLY public.t_user 
ADD 
  CONSTRAINT pk_user_table PRIMARY KEY (pk_user_id);
--
-- Name: t_user users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE 
  ONLY public.t_user 
ADD 
  CONSTRAINT users_email_key UNIQUE (email);
--
-- Name: t_user users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE 
  ONLY public.t_user 
ADD 
  CONSTRAINT users_username_key UNIQUE (username);
--
-- Name: t_repository update_t_repository_gmt_updated; Type: TRIGGER; Schema: public; Owner: postgres
--
CREATE TRIGGER update_t_repository_gmt_updated BEFORE 
UPDATE 
  ON public.t_repository FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();
--
-- Name: t_user update_t_user_gmt_updated; Type: TRIGGER; Schema: public; Owner: postgres
--
CREATE TRIGGER update_t_user_gmt_updated BEFORE 
UPDATE 
  ON public.t_user FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();
--
-- Name: t_user_repository update_t_user_repository_gmt_updated; Type: TRIGGER; Schema: public; Owner: postgres
--
CREATE TRIGGER update_t_user_repository_gmt_updated BEFORE 
UPDATE 
  ON public.t_user_repository FOR EACH ROW EXECUTE FUNCTION public.update_gmt_updated_column();
