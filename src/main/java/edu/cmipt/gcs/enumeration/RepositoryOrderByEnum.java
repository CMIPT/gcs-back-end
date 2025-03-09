package edu.cmipt.gcs.enumeration;

public enum RepositoryOrderByEnum {
  STAR,
  FORK,
  WATCHER,
  GMT_CREATED,
  REPOSITORY_NAME;

  public String getFieldName() {
    return this.name().toLowerCase();
  }
}
