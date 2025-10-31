package tech.wetech.flexmodel.interfaces.rest.vertx;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.ApiRuntimeApplicationService;
import tech.wetech.flexmodel.application.dto.GraphQLRefreshEvent;
import tech.wetech.flexmodel.shared.FlexmodelConfig;



/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class FlexmodelAPIHandler {

  @Inject
  ApiRuntimeApplicationService apiRuntimeApplicationService;
  @Inject
  EventBus eventBus;
  @Inject
  FlexmodelConfig config;

  void handle(@Observes StartupEvent startupEvent, Router router) {
    // 处理所有以"/api/v1"开头的请求
    router.route()
      .handler(BodyHandler.create())
      .pathRegex(config.apiRootPath() + "/.*")
      .blockingHandler(apiRuntimeApplicationService::accept);

    router.route().pathRegex("/f/datasources.*")
      .handler(handle -> {

        handle.addEndHandler(v -> {
          if (handle.request().method() != HttpMethod.GET) {
            log.info(">>> Refreshing GraphQL schema...");
            eventBus.send("graphql.refresh", new GraphQLRefreshEvent());
          }
        });
        handle.next();
      });
  }

}
