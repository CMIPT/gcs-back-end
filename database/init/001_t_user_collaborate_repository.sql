CREATE TABLE public.t_user_collaborate_repository (
  id bigint NOT NULL,
  collaborator_id bigint NOT NULL,
  repository_id bigint NOT NULL,
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_user_collaborate_repository IS 'Table for collaboration relationship.';

COMMENT ON COLUMN public.t_user_collaborate_repository.id IS 'Primary key of the collaboration relationship table.';
COMMENT ON COLUMN public.t_user_collaborate_repository.collaborator_id IS 'ID of the collaborator.';
COMMENT ON COLUMN public.t_user_collaborate_repository.repository_id IS 'ID of the repository.';
COMMENT ON COLUMN public.t_user_collaborate_repository.gmt_created IS 'Timestamp when the relationship was created.';
COMMENT ON COLUMN public.t_user_collaborate_repository.gmt_updated IS 'Timestamp when the relationship was last updated.';
COMMENT ON COLUMN public.t_user_collaborate_repository.gmt_deleted IS 'Timestamp when the relationship was deleted.
If set to NULL, it indicates that the repository has not been deleted.';
