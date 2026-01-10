package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.codegen.entity.Project;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/v1/projects")
public class ProjectResource {

  @Inject
  AuthApplicationService authApplicationService;

  @GET
  public List<Project> findProjects() {
    return authApplicationService.findProjects();
  }

  @GET
  @Path("/{projectId}")
  public Project findProject(@PathParam("projectId") String projectId) {
    return authApplicationService.findProject(projectId);
  }

}
