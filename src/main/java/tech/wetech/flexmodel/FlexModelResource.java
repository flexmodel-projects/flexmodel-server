package tech.wetech.flexmodel;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author cjbi
 */
@Path("/flexmodel")
public class FlexModelResource {

  @Inject
  Session session;

  @GET
  @Path("/list")
  public Object list() {
    return session.find("Student", query -> query);
  }

}
