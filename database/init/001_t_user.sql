CREATE TABLE public.t_user (
  id bigint NOT NULL,
  username character varying(50) NOT NULL,
  email character varying(254) NOT NULL,
  user_password character(32) NOT NULL,
  gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
  gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_user IS 'Table for storing user information.';

COMMENT ON COLUMN public.t_user.id IS 'Primary key of the user table.';
COMMENT ON COLUMN public.t_user.username IS 'Username of the user.';
COMMENT ON COLUMN public.t_user.email IS 'Email address of the user.';
COMMENT ON COLUMN public.t_user.user_password IS 'Password of the user, stored as an MD5 hash.';
COMMENT ON COLUMN public.t_user.gmt_created IS 'Timestamp when the user record was created.';
COMMENT ON COLUMN public.t_user.gmt_updated IS 'Timestamp when the user record was last updated.';
COMMENT ON COLUMN public.t_user.gmt_deleted IS 'Timestamp when the user record was deleted.
If set to NULL, it indicates that the user information has not been deleted.';
