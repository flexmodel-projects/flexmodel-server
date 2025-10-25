package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.event.EventListener;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.event.impl.PreInsertEvent;
import tech.wetech.flexmodel.event.impl.PreUpdateEvent;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.shared.SessionContextHolder;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

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
    if (newData == null) {
      return;
    }
    SessionFactory sf = event.getSource();
    EntityDefinition entity = (EntityDefinition) sf.getModelRegistry().getRegistered(event.getSchemaName(), event.getModelName());
    TypedField<?, ?> tenantIdField = entity.getField("tenant_id");
    TypedField<?, ?> createdByField = entity.getField("created_by");
    TypedField<?, ?> updatedByField = entity.getField("updated_by");
    TypedField<?, ?> createdAtField = entity.getField("created_at");
    TypedField<?, ?> updatedAtField = entity.getField("updated_at");
    if (tenantIdField != null && newData.get("tenant_id") == null) {
      newData.put("tenant_id", SessionContextHolder.getTenantId());
    }
    if (event instanceof PreInsertEvent) {
      if (createdByField != null && newData.get("created_by") == null) {
        newData.put("created_by", SessionContextHolder.getUserId());
      }
      if (createdAtField != null && newData.get("created_at") == null) {
        newData.put("created_at", LocalDateTime.now());
      }
    }
    if (event instanceof PreUpdateEvent) {
      if (updatedByField != null && newData.get("updated_by") == null) {
        newData.put("updated_by", SessionContextHolder.getUserId());
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
    String tenantId = SessionContextHolder.getTenantId();
    if (tenantId == null) {
      return;
    }
    SessionFactory sf = event.getSource();
    EntityDefinition entity = (EntityDefinition) sf.getModelRegistry().getRegistered(event.getSchemaName(), event.getModelName());
    TypedField<?, ?> tenantIdField = entity.getField("tenant_id");
    if (tenantIdField == null) {
      return;
    }
    List<Map<String, Object>> andList = new ArrayList<>();
    andList.add(Map.of("tenant_id", Map.of("_eq", tenantId)));
    if (query.getFilter() != null) {
      @SuppressWarnings("all")
      Map<String, Object> filterMap = JsonUtils.getInstance().parseToObject(query.getFilter(), Map.class);
      andList.add(filterMap);
    }
    query.setFilter(JsonUtils.getInstance().stringify(Map.of("_and", andList)));
  }

}
