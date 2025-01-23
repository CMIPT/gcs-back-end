CREATE TABLE public.t_ssh_key (
  id bigint NOT NULL,
  user_id bigint NOT NULL,
  name character varying(255) NOT NULL,
  public_key character varying(4096) NOT NULL,
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_ssh_key IS 'Table for storing ssh public key.';
COMMENT ON COLUMN public.t_ssh_key.id IS 'Primary key of the ssh_key table.';
COMMENT ON COLUMN public.t_ssh_key.user_id IS 'ID of the user who owns the ssh key.';
COMMENT ON COLUMN public.t_ssh_key.name IS 'Name of the ssh key.';
COMMENT ON COLUMN public.t_ssh_key.public_key IS 'Public key of the ssh key.';
COMMENT ON COLUMN public.t_ssh_key.gmt_created IS 'Timestamp when the ssh_key record was created.';
COMMENT ON COLUMN public.t_ssh_key.gmt_updated IS 'Timestamp when the ssh_key record was last updated.';
COMMENT ON COLUMN public.t_ssh_key.gmt_deleted IS 'Timestamp when the ssh_key record was deleted.
If set to NULL, it indicates that the ssh_key record has not been deleted.';
