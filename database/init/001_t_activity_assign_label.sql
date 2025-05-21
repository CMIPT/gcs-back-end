CREATE TABLE public.t_activity_assign_label (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    activity_id bigint NOT NULL, -- 对应t_activity表的id
    label_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_activity_assign_label IS 'Association table between activity and label.';
COMMENT ON COLUMN public.t_activity_assign_label.id IS 'Primary key of the activity_label table.';
COMMENT ON COLUMN public.t_activity_assign_label.user_id IS 'User ID of the label creator.';
COMMENT ON COLUMN public.t_activity_assign_label.activity_id IS 'ID of the activity.';
COMMENT ON COLUMN public.t_activity_assign_label.label_id IS 'ID of the label associated with the activity.';
COMMENT ON COLUMN public.t_activity_assign_label.gmt_created IS 'Timestamp when the relationship was created.';
COMMENT ON COLUMN public.t_activity_assign_label.gmt_updated IS 'Timestamp when the relationship was last updated.';
COMMENT ON COLUMN public.t_activity_assign_label.gmt_deleted IS 'Timestamp when the relationship was deleted.
If set to NULL, it indicates that the relationship has not been deleted.';
