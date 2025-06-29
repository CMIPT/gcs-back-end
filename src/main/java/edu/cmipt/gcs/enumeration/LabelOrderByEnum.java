package edu.cmipt.gcs.enumeration;


public enum LabelOrderByEnum {
  NAME,
  GMT_CREATED;

  public String getFieldName() {
    return this.name().toLowerCase();
  }
}
