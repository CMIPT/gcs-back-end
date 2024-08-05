CREATE TABLE public.t_user_repository (
  pk_user_repository_id bigint DEFAULT nextval('public.user_repository_id_seq'::regclass) NOT NULL, 
  user_id bigint NOT NULL, 
  repository_id bigint NOT NULL, 
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL, 
  gmt_deleted timestamp without time zone
);

ALTER TABLE public.t_user_repository OWNER TO postgres;

COMMENT ON TABLE public.t_user_repository IS 'Table for storing relationships between users and repositories.';

COMMENT ON COLUMN public.t_user_repository.pk_user_repository_id IS 'Primary key of the user_repository table.';
COMMENT ON COLUMN public.t_user_repository.user_id IS 'ID of the user.';
COMMENT ON COLUMN public.t_user_repository.repository_id IS 'ID of the repository.';
COMMENT ON COLUMN public.t_user_repository.gmt_created IS 'Timestamp when the relationship was created.';
COMMENT ON COLUMN public.t_user_repository.gmt_updated IS 'Timestamp when the relationship was last updated.';
COMMENT ON COLUMN public.t_user_repository.gmt_deleted IS 'Timestamp when the relationship was deleted.';
