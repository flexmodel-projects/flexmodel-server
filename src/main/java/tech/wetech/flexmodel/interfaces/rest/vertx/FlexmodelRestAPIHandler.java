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
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.application.dto.GraphQLRefreshEvent;
import tech.wetech.flexmodel.codegen.entity.Project;
import tech.wetech.flexmodel.shared.FlexmodelConfig;

import java.util.List;


/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class FlexmodelRestAPIHandler {

  @Inject
  ApiRuntimeApplicationService apiRuntimeApplicationService;
  @Inject
  EventBus eventBus;
  @Inject
  FlexmodelConfig config;
  @Inject
  AuthApplicationService authApplicationService;

  void handle(@Observes StartupEvent startupEvent, Router router) {

    List<Project> projects = authApplicationService.findProjects();
    for (Project project : projects) {
      router.route()
        .pathRegex(config.apiRootPath() + "/" + project.getId() + "/.*")
        .handler(BodyHandler.create())
        .blockingHandler(apiRuntimeApplicationService::accept);
    }

    router.route().pathRegex("/v1/datasources.*")
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
