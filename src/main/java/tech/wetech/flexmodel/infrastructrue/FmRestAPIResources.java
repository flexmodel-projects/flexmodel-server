package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.FlexmodelConfig;
import tech.wetech.flexmodel.application.ApiRuntimeApplicationService;
import tech.wetech.flexmodel.graphql.GraphQLProvider;

/**
 * @author cjbi
 */
@Slf4j
public class FmRestAPIResources {

  @Inject
  ApiRuntimeApplicationService apiRuntimeApplicationService;

  @Inject
  FlexmodelConfig config;

  void installRoute(@Observes StartupEvent startupEvent, Router router, GraphQLProvider graphQLProvider) {
    // 处理所有以"/api/v1"开头的请求
    router.route()
      .handler(BodyHandler.create())
      .pathRegex(config.contextPath() + "/.*")
      .blockingHandler(apiRuntimeApplicationService::accept);

    router.route().pathRegex("/api/datasources.*")
      .handler(handle -> {

        handle.addEndHandler(v -> {
          if (handle.request().method() != HttpMethod.GET) {
            log.info(">>> Refreshing GraphQL schema...");
            graphQLProvider.init();
          }
        });
        handle.next();
      });
  }

}
