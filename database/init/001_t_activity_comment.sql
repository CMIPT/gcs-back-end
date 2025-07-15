CREATE TABLE public.t_activity_comment (
    id bigint NOT NULL,
    activity_id bigint NOT NULL,
    user_id bigint NOT NULL,
    content text NOT NULL ,
    code_path character varying(1024),
    code_line int,
    reply_to_id bigint,
    gmt_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    gmt_resolved timestamp without time zone,
    gmt_hidden timestamp without time zone,
    gmt_deleted timestamp without time zone
);

COMMENT ON TABLE public.t_activity_comment IS 'Table for storing comments on activity.';

COMMENT ON COLUMN public.t_activity_comment.id IS 'Primary key of the activity_comment table.';
COMMENT ON COLUMN public.t_activity_comment.activity_id IS 'ID of the activity this comment belongs to.';
COMMENT ON COLUMN public.t_activity_comment.user_id IS 'ID of the user who posted this comment.';
COMMENT ON COLUMN public.t_activity_comment.content IS 'Content of the comment.';
COMMENT ON COLUMN public.t_activity_comment.code_path IS 'Path of the code file where the comment is made. NULL if not applicable.';
COMMENT ON COLUMN public.t_activity_comment.code_line IS 'Line number in the code file where the comment is made. NULL if not applicable.';
COMMENT ON COLUMN public.t_activity_comment.reply_to_id IS 'Comment ID of this comment replies. NULL if this comment is not a reply to another comment.';
COMMENT ON COLUMN public.t_activity_comment.gmt_created IS 'Timestamp when the comment was created.';
COMMENT ON COLUMN public.t_activity_comment.gmt_updated IS 'Timestamp when the comment was last updated.';
COMMENT ON COLUMN public.t_activity_comment.gmt_resolved IS 'Timestamp when the comment was resolved. NULL if not resolved.';
COMMENT ON COLUMN public.t_activity_comment.gmt_hidden IS 'Timestamp when the comment was hidden. NULL if not hidden.';
COMMENT ON COLUMN public.t_activity_comment.gmt_deleted IS 'Timestamp when the comment was deleted. NULL if not deleted.';

-- The constraint of t_activity_comment is added to the table.
ALTER TABLE ONLY public.t_activity_comment
    ADD CONSTRAINT pk_activity_comment PRIMARY KEY (id);