package edu.cmipt.gcs.enumeration;

import java.util.List;

public enum LabelOrderByEnum {
    NAME,
    GMT_CREATED;

    public String getFieldName() {
        return this.name().toLowerCase();
    }
}
