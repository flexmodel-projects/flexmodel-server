package tech.wetech.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.JobApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.codegen.entity.JobExecutionLog;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@ApplicationScoped
@Tag(name = "【Flexmodel】任务", description = "任务管理")
@Path("/f/jobs")
public class JobResource {

  @Inject
  JobApplicationService jobApplicationService;

  @GET
  @Path("/logs")
  public PageDTO<JobExecutionLog> findLogPage(@QueryParam("triggerId") String triggerId,
                                              @QueryParam("jobId") String jobId,
                                              @QueryParam("status") String status,
                                              @QueryParam("startTime") LocalDateTime startTime,
                                              @QueryParam("endTime") LocalDateTime endTime,
                                              @QueryParam("isSuccess") Boolean isSuccess,
                                              @QueryParam("page") @DefaultValue("1") Integer page,
                                              @QueryParam("size") @DefaultValue("20") Integer size) {
    return jobApplicationService.findLogPage(triggerId, jobId, status, startTime, endTime, isSuccess, page, size);
  }

}
