CREATE TABLE public.t_activity_designate_assignee (
    id bigint NOT NULL,
    activity_id bigint NOT NULL,
    assigner_id bigint NOT NULL,
    assignee_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_activity_designate_assignee IS 'Association table between activity and assignee.';
COMMENT ON COLUMN public.t_activity_designate_assignee.id IS 'Primary key of the activity_designate_assignee table.';
COMMENT ON COLUMN public.t_activity_designate_assignee.activity_id IS 'ID of the activity.';
COMMENT ON COLUMN public.t_activity_designate_assignee.assigner_id IS 'ID of the user who designates the assignee.';
COMMENT ON COLUMN public.t_activity_designate_assignee.assignee_id IS 'ID of the user who is designated as the assignee.';
COMMENT ON COLUMN public.t_activity_designate_assignee.gmt_created IS 'Timestamp when the relationship was created.';
COMMENT ON COLUMN public.t_activity_designate_assignee.gmt_updated IS 'Timestamp when the relationship was last updated.';
COMMENT ON COLUMN public.t_activity_designate_assignee.gmt_deleted IS 'Timestamp when the relationship was deleted.
If set to NULL, it indicates that the relationship has not been deleted.';

