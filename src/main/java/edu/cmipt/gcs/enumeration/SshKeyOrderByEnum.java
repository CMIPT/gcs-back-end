package edu.cmipt.gcs.enumeration;

public enum SshKeyOrderByEnum {
  TITLE,
  PUBLIC_KEY,
  GMT_UPDATED,
  GMT_CREATED;

  public String getFieldName() {
    return this.name().toLowerCase();
  }
}
