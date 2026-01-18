package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.event.EventListener;
import dev.flexmodel.event.PreChangeEvent;
import dev.flexmodel.event.impl.PreInsertEvent;
import dev.flexmodel.event.impl.PreUpdateEvent;
import dev.flexmodel.model.EntityDefinition;
import dev.flexmodel.model.field.TypedField;
import dev.flexmodel.query.Query;
import dev.flexmodel.session.SessionFactory;
import dev.flexmodel.shared.SessionContextHolder;
import dev.flexmodel.shared.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class AuditDataEventListener implements EventListener {
  @Override
  public void onPreChange(PreChangeEvent event) {
    if (!"system".equals(event.getSchemaName())) {
      return;
    }
    // 拦截查询
    invokeQuery(event);
    // 拦截数据
    invokeData(event);
  }

  private void invokeData(PreChangeEvent event) {
    Map<String, Object> newData = event.getNewData();
    String projectId = SessionContextHolder.getProjectId();
    String userId = SessionContextHolder.getUserId();
    if (newData == null) {
      return;
    }
    SessionFactory sf = event.getSource();
    EntityDefinition entity = (EntityDefinition) sf.getModelRegistry().getRegistered(event.getSchemaName(), event.getModelName());
    TypedField<?, ?> projectIdField = entity.getField("tenant_id");
    TypedField<?, ?> createdByField = entity.getField("created_by");
    TypedField<?, ?> updatedByField = entity.getField("updated_by");
    TypedField<?, ?> createdAtField = entity.getField("created_at");
    TypedField<?, ?> updatedAtField = entity.getField("updated_at");
    if (projectIdField != null && newData.get("tenant_id") == null && projectId != null) {
      newData.put("tenant_id", projectId);
    }
    if (event instanceof PreInsertEvent) {
      if (createdByField != null && newData.get("created_by") == null && userId != null) {
        newData.put("created_by", userId);
      }
      if (createdAtField != null && newData.get("created_at") == null) {
        newData.put("created_at", LocalDateTime.now());
      }
    }
    if (event instanceof PreUpdateEvent) {
      if (updatedByField != null && newData.get("updated_by") == null && userId != null) {
        newData.put("updated_by", userId);
      }
      if (updatedAtField != null && newData.get("updated_at") == null) {
        newData.put("updated_at", LocalDateTime.now());
      }
    }
  }

  private void invokeQuery(PreChangeEvent event) {
    Query query = event.getQuery();
    if (query == null) {
      return;
    }
    String projectId = SessionContextHolder.getProjectId();
    if (projectId == null) {
      return;
    }
    SessionFactory sf = event.getSource();
    EntityDefinition entity = (EntityDefinition) sf.getModelRegistry().getRegistered(event.getSchemaName(), event.getModelName());
    TypedField<?, ?> projectIdField = entity.getField("tenant_id");
    if (projectIdField == null) {
      return;
    }
    List<Map<String, Object>> andList = new ArrayList<>();
    andList.add(Map.of("tenant_id", Map.of("_eq", projectId)));
    if (query.getFilter() != null) {
      @SuppressWarnings("all")
      Map<String, Object> filterMap = JsonUtils.getInstance().parseToObject(query.getFilter(), Map.class);
      andList.add(filterMap);
    }
    query.setFilter(JsonUtils.getInstance().stringify(Map.of("_and", andList)));
  }

}
