package dev.flexmodel.interfaces.rest.vertx;

import dev.flexmodel.application.ProjectApplicationService;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.application.ApiRuntimeApplicationService;
import dev.flexmodel.application.AuthApplicationService;
import dev.flexmodel.application.dto.GraphQLRefreshEvent;
import dev.flexmodel.application.dto.ProjectListRequest;
import dev.flexmodel.application.dto.ProjectResponse;
import dev.flexmodel.codegen.entity.Project;
import dev.flexmodel.shared.FlexmodelConfig;

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
  ProjectApplicationService projectApplicationService;

  void handle(@Observes StartupEvent startupEvent, Router router) {
    List<ProjectResponse> projects = projectApplicationService.findProjects(new ProjectListRequest(null));
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
