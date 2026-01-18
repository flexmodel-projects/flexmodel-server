package dev.flexmodel.interfaces.rest.vertx;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class StaticResourceHandler {

  @ConfigProperty(name = "quarkus.http.root-path")
  String rootPath;

  public void handle(@Observes StartupEvent startupEvent, Router router) {
    router.route()
      .path("/flexmodel-ui/*")
      .handler(ctx -> {
        String resource = ctx.pathParam("*");
        ctx.reroute(rootPath + "/webjars/flexmodel-ui/" + resource);
      });
  }

}
