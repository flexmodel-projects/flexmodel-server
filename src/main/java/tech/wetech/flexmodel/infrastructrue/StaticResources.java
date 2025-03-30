package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;

/**
 * @author cjbi
 */
public class StaticResources {

  void installRoute(@Observes StartupEvent startupEvent, Router router) {
    router.route()
      .path("/fm-ui/*")
      .handler(ctx -> {
        String resource = ctx.pathParam("*");
        ctx.reroute("/webjars/flexmodel-ui/" + resource);
      });
  }

}
