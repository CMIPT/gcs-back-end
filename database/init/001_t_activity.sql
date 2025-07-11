CREATE TABLE public.t_activity (
    id bigint NOT NULL,
    number int NOT NULL,
    repository_id bigint NOT NULL,
    parent_id bigint,
    title character varying(255) NOT NULL,
    description text default '' NOT NULL,
    is_pull_request boolean NOT NULL, -- false: issue, true: pull_request
    user_id bigint NOT NULL,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_closed timestamp without time zone,
    gmt_locked timestamp without time zone,
    gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_activity IS 'Table for storing issue or pull_request information in repositories.';

COMMENT ON COLUMN public.t_activity.id IS 'Primary key of the activity table.';
COMMENT ON COLUMN public.t_activity.number IS 'Sequence number of the activity.';
COMMENT ON COLUMN public.t_activity.repository_id IS 'ID of the repository this activity belongs to.';
COMMENT ON COLUMN public.t_activity.parent_id IS 'ID of the parent activity. NULL if this activity is not a reply to another activity.';
COMMENT ON COLUMN public.t_activity.title IS 'Title of the activity.';
COMMENT ON COLUMN public.t_activity.description IS 'Detailed description of the activity.';
COMMENT ON COLUMN public.t_activity.is_pull_request IS 'Type of the activity. False: issue, True: pull_request.';
COMMENT ON COLUMN public.t_activity.user_id IS 'ID of the user who created this issue or this pull_request.';
COMMENT ON COLUMN public.t_activity.gmt_created IS 'Timestamp when the activity was created.';
COMMENT ON COLUMN public.t_activity.gmt_updated IS 'Timestamp when the activity was last updated.';
COMMENT ON COLUMN public.t_activity.gmt_closed IS 'Timestamp when the activity was closed. NULL if still open.';
COMMENT ON COLUMN public.t_activity.gmt_locked IS 'Timestamp when the activity was locked. NULL if not locked.';
COMMENT ON COLUMN public.t_activity.gmt_deleted IS 'Timestamp when the activity was deleted. NULL if not deleted.';

-- The constraint of t_activity is added to the table.
ALTER TABLE ONLY public.t_activity
    ADD CONSTRAINT pk_activity PRIMARY KEY (id);
CREATE UNIQUE INDEX uniq_t_activity_number_repository_id
    ON public.t_activity(number, repository_id)