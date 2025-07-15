package edu.cmipt.gcs.enumeration;

public enum ActivityOrderByEnum {
  GMT_CREATED,
  GMT_UPDATED,
  TOTAL_COMMENTS,
  GMT_COMMENT_UPDATED; //TODO: 暂不支持按照评论数和最新评论排序
}
