package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.application.dto.ProjectListRequest;
import tech.wetech.flexmodel.application.dto.ProjectResponse;
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
  public List<ProjectResponse> findProjects(@QueryParam("include") String include) {
    return authApplicationService.findProjects(new ProjectListRequest(include));
  }

  @GET
  @Path("/{projectId}")
  public Project findProject(@PathParam("projectId") String projectId) {


    return authApplicationService.findProject(projectId);
  }

}
