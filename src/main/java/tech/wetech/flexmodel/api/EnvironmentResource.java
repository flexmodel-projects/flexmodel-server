package tech.wetech.flexmodel.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Path("/api/environment")
public class EnvironmentResource {

  @GET
  @Path("/variables")
  public Map<String, Object> getSystemVariables() {
    Map<String, Object> all = new HashMap<>();
    all.put("environment",  System.getenv());
    all.put("system", System.getProperties());
    return all;
  }

}
