--
-- PostgreSQL database dump
--

-- Dumped from database version 15.7
-- Dumped by pg_dump version 16.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION adminpack; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';


--
-- Name: issues_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.issues_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.issues_id_seq OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: issue; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.issue (
    pk_issue_id bigint DEFAULT nextval('public.issues_id_seq'::regclass) NOT NULL,
    title character varying(100) NOT NULL,
    description text,
    repository_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_deleted timestamp without time zone,
    CONSTRAINT issue_pk_issue_id_check CHECK ((pk_issue_id >= 0)),
    CONSTRAINT issue_repository_id_check CHECK ((repository_id >= 0))
);


ALTER TABLE public.issue OWNER TO postgres;

--
-- Name: TABLE issue; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.issue IS 'Table for storing issues related to repositories.';


--
-- Name: COLUMN issue.pk_issue_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.pk_issue_id IS 'Primary key of the issue table.';


--
-- Name: COLUMN issue.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.title IS 'Title of the issue.';


--
-- Name: COLUMN issue.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.description IS 'Description of the issue.';


--
-- Name: COLUMN issue.repository_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.repository_id IS 'ID of the repository to which the issue belongs.';


--
-- Name: COLUMN issue.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.gmt_created IS 'Timestamp when the issue was created.';


--
-- Name: COLUMN issue.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.gmt_updated IS 'Timestamp when the issue was last updated.';


--
-- Name: COLUMN issue.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.issue.gmt_deleted IS 'Timestamp when the issue was deleted.';


--
-- Name: repositories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.repositories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.repositories_id_seq OWNER TO postgres;

--
-- Name: repository; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository (
    pk_repository_id bigint DEFAULT nextval('public.repositories_id_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    is_private boolean DEFAULT false,
    user_id bigint,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_deleted timestamp without time zone,
    CONSTRAINT repository_pk_repository_id_check CHECK ((pk_repository_id >= 0)),
    CONSTRAINT repository_user_id_check CHECK ((user_id >= 0))
);


ALTER TABLE public.repository OWNER TO postgres;

--
-- Name: TABLE repository; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.repository IS 'Table for storing repository information.';


--
-- Name: COLUMN repository.pk_repository_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.pk_repository_id IS 'Primary key of the repository table.';


--
-- Name: COLUMN repository.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.name IS 'Name of the repository.';


--
-- Name: COLUMN repository.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.description IS 'Description of the repository.';


--
-- Name: COLUMN repository.is_private; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.is_private IS 'Indicates if the repository is private.';


--
-- Name: COLUMN repository.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.user_id IS 'ID of the user who owns the repository.';


--
-- Name: COLUMN repository.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.gmt_created IS 'Timestamp when the repository was created.';


--
-- Name: COLUMN repository.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.gmt_updated IS 'Timestamp when the repository was last updated.';


--
-- Name: COLUMN repository.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository.gmt_deleted IS 'Timestamp when the repository was deleted.';


--
-- Name: repository_issue; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository_issue (
    pk_repository_issue_id integer NOT NULL,
    repository_id bigint NOT NULL,
    issue_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_deleted timestamp without time zone,
    CONSTRAINT repository_issue_issue_id_check CHECK ((issue_id >= 0)),
    CONSTRAINT repository_issue_repository_id_check CHECK ((repository_id >= 0))
);


ALTER TABLE public.repository_issue OWNER TO postgres;

--
-- Name: TABLE repository_issue; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.repository_issue IS 'Table for storing relationships between repositories and issues.';


--
-- Name: COLUMN repository_issue.pk_repository_issue_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.pk_repository_issue_id IS 'Primary key of the repository_issue table.';


--
-- Name: COLUMN repository_issue.repository_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.repository_id IS 'ID of the repository.';


--
-- Name: COLUMN repository_issue.issue_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.issue_id IS 'ID of the issue.';


--
-- Name: COLUMN repository_issue.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.gmt_created IS 'Timestamp when the relationship was created.';


--
-- Name: COLUMN repository_issue.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.gmt_updated IS 'Timestamp when the relationship was last updated.';


--
-- Name: COLUMN repository_issue.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.repository_issue.gmt_deleted IS 'Timestamp when the relationship was deleted.';


--
-- Name: repository_issue_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.repository_issue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.repository_issue_id_seq OWNER TO postgres;

--
-- Name: repository_issue_pk_repository_issue_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.repository_issue_pk_repository_issue_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.repository_issue_pk_repository_issue_id_seq OWNER TO postgres;

--
-- Name: repository_issue_pk_repository_issue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.repository_issue_pk_repository_issue_id_seq OWNED BY public.repository_issue.pk_repository_issue_id;


--
-- Name: user_repository; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_repository (
    pk_user_repository_id integer NOT NULL,
    user_id bigint NOT NULL,
    repository_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_deleted timestamp without time zone,
    CONSTRAINT user_repository_repository_id_check CHECK ((repository_id >= 0)),
    CONSTRAINT user_repository_user_id_check CHECK ((user_id >= 0))
);


ALTER TABLE public.user_repository OWNER TO postgres;

--
-- Name: TABLE user_repository; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_repository IS 'Table for storing relationships between users and repositories.';


--
-- Name: COLUMN user_repository.pk_user_repository_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.pk_user_repository_id IS 'Primary key of the user_repository table.';


--
-- Name: COLUMN user_repository.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.user_id IS 'ID of the user.';


--
-- Name: COLUMN user_repository.repository_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.repository_id IS 'ID of the repository.';


--
-- Name: COLUMN user_repository.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.gmt_created IS 'Timestamp when the relationship was created.';


--
-- Name: COLUMN user_repository.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.gmt_updated IS 'Timestamp when the relationship was last updated.';


--
-- Name: COLUMN user_repository.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_repository.gmt_deleted IS 'Timestamp when the relationship was deleted.';


--
-- Name: user_repository_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_repository_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_repository_id_seq OWNER TO postgres;

--
-- Name: user_repository_pk_user_repository_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_repository_pk_user_repository_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_repository_pk_user_repository_id_seq OWNER TO postgres;

--
-- Name: user_repository_pk_user_repository_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_repository_pk_user_repository_id_seq OWNED BY public.user_repository.pk_user_repository_id;


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: user_table; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_table (
    pk_user_id bigint DEFAULT nextval('public.users_id_seq'::regclass) NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(50) NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gmt_deleted timestamp without time zone,
    CONSTRAINT user_pk_user_id_check CHECK ((pk_user_id >= 0))
);


ALTER TABLE public.user_table OWNER TO postgres;

--
-- Name: TABLE user_table; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_table IS 'Table for storing user information.';


--
-- Name: COLUMN user_table.pk_user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.pk_user_id IS 'Primary key of the user table.';


--
-- Name: COLUMN user_table.username; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.username IS 'Username of the user.';


--
-- Name: COLUMN user_table.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.email IS 'Email address of the user.';


--
-- Name: COLUMN user_table.gmt_created; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.gmt_created IS 'Timestamp when the user record was created.';


--
-- Name: COLUMN user_table.gmt_updated; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.gmt_updated IS 'Timestamp when the user record was last updated.';


--
-- Name: COLUMN user_table.gmt_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_table.gmt_deleted IS 'Timestamp when the user record was deleted.';


--
-- Name: repository_issue pk_repository_issue_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_issue ALTER COLUMN pk_repository_issue_id SET DEFAULT nextval('public.repository_issue_pk_repository_issue_id_seq'::regclass);


--
-- Name: user_repository pk_user_repository_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_repository ALTER COLUMN pk_user_repository_id SET DEFAULT nextval('public.user_repository_pk_user_repository_id_seq'::regclass);


--
-- Name: issue pk_issue; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.issue
    ADD CONSTRAINT pk_issue PRIMARY KEY (pk_issue_id);


--
-- Name: repository pk_repository; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository
    ADD CONSTRAINT pk_repository PRIMARY KEY (pk_repository_id);


--
-- Name: user_table pk_user; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT pk_user PRIMARY KEY (pk_user_id);


--
-- Name: repository_issue repository_issue_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_issue
    ADD CONSTRAINT repository_issue_pkey PRIMARY KEY (pk_repository_issue_id);


--
-- Name: user_repository user_repository_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_repository
    ADD CONSTRAINT user_repository_pkey PRIMARY KEY (pk_user_repository_id);


--
-- Name: user_table users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: user_table users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- PostgreSQL database dump complete
--

