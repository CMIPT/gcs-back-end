CREATE TABLE public.t_user_star_repository (
  id bigint NOT NULL,
  user_id bigint NOT NULL,
  repository_id bigint NOT NULL,
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_user_star_repository IS 'Table for storing relationships between users and starred repositories.';

COMMENT ON COLUMN public.t_user_star_repository.id IS 'Primary key of the user_star_repository table.';
COMMENT ON COLUMN public.t_user_star_repository.user_id IS 'ID of the user who starred the repository.';
COMMENT ON COLUMN public.t_user_star_repository.repository_id IS 'ID of the repository that has been starred.';
COMMENT ON COLUMN public.t_user_star_repository.gmt_created IS 'Timestamp when the relationship was created.';
COMMENT ON COLUMN public.t_user_star_repository.gmt_updated IS 'Timestamp when the relationship was last updated.';
COMMENT ON COLUMN public.t_user_star_repository.gmt_deleted IS 'Timestamp when the relationship was deleted.
If set to NULL, it indicates that this relationship has not been deleted.';

-- The constraint of t_user_star_repository is added to the table.
ALTER TABLE ONLY public.t_user_star_repository
    ADD CONSTRAINT pk_user_star_repository PRIMARY KEY (id);
ALTER TABLE ONLY public.t_user_star_repository
    ADD CONSTRAINT unique_t_user_star_repository_user_id_repository_id
    UNIQUE (user_id, repository_id, gmt_deleted);