CREATE TABLE public.t_repository (
  id bigint NOT NULL,
  repository_name character varying(255) NOT NULL,
  repository_description character varying(255) NOT NULL,
  is_private boolean DEFAULT false,
  user_id bigint NOT NULL,
  star integer DEFAULT 0 NOT NULL,
  fork integer DEFAULT 0 NOT NULL,
  watcher integer DEFAULT 0 NOT NULL,
  https_url character varying(1024) NOT NULL,
  ssh_url character varying(1024) NOT NULL,
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_repository IS 'Table for storing repository information.';

COMMENT ON COLUMN public.t_repository.id IS 'Primary key of the repository table.';
COMMENT ON COLUMN public.t_repository.repository_name IS 'Name of the repository.';
COMMENT ON COLUMN public.t_repository.repository_description IS 'Description of the repository.';
COMMENT ON COLUMN public.t_repository.is_private IS 'Indicates if the repository is private.';
COMMENT ON COLUMN public.t_repository.user_id IS 'ID of the user who owns the repository.';
COMMENT ON COLUMN public.t_repository.star IS 'Number of stars the repository has received.';
COMMENT ON COLUMN public.t_repository.fork IS 'Number of times the repository has been forked.';
COMMENT ON COLUMN public.t_repository.watcher IS 'Number of users watching the repository.';
COMMENT ON COLUMN public.t_repository.https_url IS 'Repository link under HTTPS protocol.';
COMMENT ON COLUMN public.t_repository.ssh_url IS 'Repository link under SSH protocol.';
COMMENT ON COLUMN public.t_repository.gmt_created IS 'Timestamp when the repository was created.';
COMMENT ON COLUMN public.t_repository.gmt_updated IS 'Timestamp when the repository was last updated.';
COMMENT ON COLUMN public.t_repository.gmt_deleted IS 'Timestamp when the repository was deleted.
If set to NULL, it indicates that the repository has not been deleted.';
