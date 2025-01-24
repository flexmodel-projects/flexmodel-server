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
      .handler(handler -> handler.reroute("/webjars/flexmodel-ui/"));
    router.route()
      .path("/assets/:resource")
      .handler(ctx -> {
        String resource = ctx.pathParam("resource");
        ctx.reroute("/webjars/flexmodel-ui/assets/" + resource);
      });
    router.route()
      .path("/swagger-ui/:resource")
      .handler(ctx -> {
        String resource = ctx.pathParam("resource");
        ctx.reroute("/webjars/flexmodel-ui/swagger-ui/" + resource);
      });
    router.route()
      .path("/graphiql/:resource")
      .handler(ctx -> {
        String resource = ctx.pathParam("resource");
        ctx.reroute("/webjars/flexmodel-ui/graphiql/" + resource);
      });
    router.route()
      .path("/rapi-doc/:resource")
      .handler(ctx -> {
        String resource = ctx.pathParam("resource");
        ctx.reroute("/webjars/flexmodel-ui/rapi-doc/" + resource);
      });
    router.route()
      .path("/logo.png")
      .handler(handler -> handler.reroute("/webjars/flexmodel-ui/logo.png"));
    router.route()
      .path("/favicon.ico")
      .handler(handler -> handler.reroute("/webjars/flexmodel-ui/favicon.ico"));
  }

}
