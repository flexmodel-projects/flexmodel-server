package dev.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.JobApplicationService;
import dev.flexmodel.application.dto.PageDTO;
import dev.flexmodel.codegen.entity.JobExecutionLog;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@ApplicationScoped
@Tag(name = "任务", description = "任务管理")
@Path("/v1/projects/{projectId}/jobs")
public class JobResource {

  @Inject
  JobApplicationService jobApplicationService;

  @GET
  @Path("/logs")
  public PageDTO<JobExecutionLog> findLogPage(@PathParam("projectId") String projectId,
                                              @QueryParam("triggerId") String triggerId,
                                              @QueryParam("jobId") String jobId,
                                              @QueryParam("status") String status,
                                              @QueryParam("startTime") LocalDateTime startTime,
                                              @QueryParam("endTime") LocalDateTime endTime,
                                              @QueryParam("isSuccess") Boolean isSuccess,
                                              @QueryParam("page") @DefaultValue("1") Integer page,
                                              @QueryParam("size") @DefaultValue("20") Integer size) {
    return jobApplicationService.findLogPage(projectId, triggerId, jobId, status, startTime, endTime, isSuccess, page, size);
  }

}
