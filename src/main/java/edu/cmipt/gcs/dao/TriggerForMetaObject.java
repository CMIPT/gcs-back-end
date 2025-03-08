package edu.cmipt.gcs.dao;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class TriggerForMetaObject implements MetaObjectHandler {

  @Override
  public void insertFill(MetaObject metaObject) {
    this.setFieldValByName("gmtCreated", Timestamp.valueOf(LocalDateTime.now()), metaObject);
    this.setFieldValByName("gmtUpdated", Timestamp.valueOf(LocalDateTime.now()), metaObject);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    this.setFieldValByName("gmtUpdated", Timestamp.valueOf(LocalDateTime.now()), metaObject);
  }
}
