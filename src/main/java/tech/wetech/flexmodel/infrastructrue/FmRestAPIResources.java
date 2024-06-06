package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.RestAPIApplicationService;

/**
 * @author cjbi
 */
public class FmRestAPIResources {

  @Inject
  RestAPIApplicationService restAPIApplicationService;

  void installRoute(@Observes StartupEvent startupEvent, Router router) {
    // 处理所有以"/api/v1"开头的请求
    router.route()
      .pathRegex("/api/v1/.*")
      .handler(restAPIApplicationService::apply);
  }

}
