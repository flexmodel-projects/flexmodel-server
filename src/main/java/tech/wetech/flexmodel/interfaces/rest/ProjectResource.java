package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.codegen.entity.Project;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/f/projects")
public class ProjectResource {

  @Inject
  AuthApplicationService authApplicationService;

  @GET
  public List<Project> findProjects() {
    return authApplicationService.findProjects();
  }

}
