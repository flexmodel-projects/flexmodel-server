package dev.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.codegen.entity.ApiDefinitionHistory;
import dev.flexmodel.domain.model.api.ApiDefinitionChangedEvent;
import dev.flexmodel.domain.model.api.ApiDefinitionService;
import dev.flexmodel.shared.utils.JsonUtils;

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
    ApiDefinitionHistory history = JsonUtils.getInstance().convertValue(apiDefinition, ApiDefinitionHistory.class);
    history.setId(null);
    history.setApiDefinitionId(apiDefinition.getId());
    apiDefinitionService.saveApiDefinitionHistory(history);
  }


}
