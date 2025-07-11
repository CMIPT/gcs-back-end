CREATE TABLE public.t_label (
     id bigint NOT NULL,
     user_id bigint NOT NULL,
     repository_id bigint NOT NULL,
     name character varying(255) NOT NULL,
     description text,
     hex_color character varying(7) NOT NULL,
     gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
     gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
     gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_label IS 'Table for storing label information.';

COMMENT ON COLUMN public.t_label.id IS 'Primary key of the label table.';
COMMENT ON COLUMN public.t_label.user_id IS 'ID of the user who created the label.';
COMMENT ON COLUMN public.t_label.repository_id IS 'ID of the repository to which the label belongs.';
COMMENT ON COLUMN public.t_label.name IS 'Name of the label.';
COMMENT ON COLUMN public.t_label.description IS 'Description of the label.';
COMMENT ON COLUMN public.t_label.hex_color IS 'Hex color of the label.';
COMMENT ON COLUMN public.t_label.gmt_created IS 'Timestamp when the label record was created.';
COMMENT ON COLUMN public.t_label.gmt_updated IS 'Timestamp when the label record was last updated.';
COMMENT ON COLUMN public.t_label.gmt_deleted IS 'Timestamp when the label record was deleted.
-- If set to NULL, it indicates that the label record has not been deleted.';

-- The constraint of t_label added to the table.
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT pk_label PRIMARY KEY (id);
CREATE UNIQUE INDEX uniq_t_label_name_repository_id_when_gmt_deleted_null
    ON public.t_label(name, repository_id)
    WHERE gmt_deleted IS NULL;
ALTER TABLE ONLY public.t_label
    ADD CONSTRAINT ck_label_color
CHECK (hex_color ~ '^#[0-9A-Fa-f]{6}$');