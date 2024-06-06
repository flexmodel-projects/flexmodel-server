package tech.wetech.flexmodel.application;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfoService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class RestAPIApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  public void apply(RoutingContext routingContext) {
//    apiInfoService.findList();
    routingContext.response().end("Matched request for path: " + routingContext.normalisedPath());
  }

}
