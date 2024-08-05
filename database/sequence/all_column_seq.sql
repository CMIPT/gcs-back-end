CREATE SEQUENCE public.issues_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.issues_id_seq OWNER TO postgres;

CREATE SEQUENCE public.repositories_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.repositories_id_seq OWNER TO postgres;

CREATE SEQUENCE public.repository_issue_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.repository_issue_id_seq OWNER TO postgres;

CREATE SEQUENCE public.users_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

CREATE SEQUENCE public.user_repository_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.user_repository_id_seq OWNER TO postgres;
