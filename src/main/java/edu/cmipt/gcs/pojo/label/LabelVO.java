package edu.cmipt.gcs.pojo.label;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Label Data Transfer Object")
public record LabelVO(String id, String name, String hexColor, String description) {
  public LabelVO(LabelPO labelPO) {
    this(
        labelPO.getId().toString(),
        labelPO.getName(),
        labelPO.getHexColor(),
        labelPO.getDescription());
  }

  public LabelVO(LabelDTO labelDTO) {
    this(labelDTO.id(), labelDTO.name(), labelDTO.hexColor(), labelDTO.description());
  }
}
