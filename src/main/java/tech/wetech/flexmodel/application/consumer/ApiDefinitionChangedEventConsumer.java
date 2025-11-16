package tech.wetech.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.entity.ApiDefinitionHistory;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionChangedEvent;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ApiDefinitionChangedEventConsumer {

  @Inject
  ApiDefinitionService apiDefinitionService;

  @ConsumeEvent("api.changed")
  public void consume(ApiDefinitionChangedEvent event) {
    log.info("ApiDefinitionChangedEvent: {}", event);
    ApiDefinition apiDefinition = event.apiDefinition();
    ApiDefinitionHistory history = buildApiDefinitionHistory(apiDefinition);
    apiDefinitionService.saveApiDefinitionHistory(history);
  }

  private ApiDefinitionHistory buildApiDefinitionHistory(ApiDefinition apiDefinition) {
    ApiDefinitionHistory history = new ApiDefinitionHistory();
    history.setApiDefinitionId(apiDefinition.getId());
    history.setName(apiDefinition.getName());
    history.setParentId(apiDefinition.getParentId());
    history.setType(apiDefinition.getType());
    history.setMethod(apiDefinition.getMethod());
    history.setPath(apiDefinition.getPath());
    history.setMeta(apiDefinition.getMeta());
    history.setEnabled(apiDefinition.getEnabled());
    history.setCreatedBy(apiDefinition.getCreatedBy());
    history.setTenantId(apiDefinition.getTenantId());
    history.setMeta(apiDefinition.getMeta());
    history.setEnabled(apiDefinition.getEnabled());
    return history;
  }

}
