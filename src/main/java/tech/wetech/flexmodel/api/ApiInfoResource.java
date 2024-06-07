package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.ApiDesignApplicationService;
import tech.wetech.flexmodel.application.dto.ApiInfoTreeDTO;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/api/apis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiInfoResource {

  @Inject
  ApiDesignApplicationService apiDesignApplicationService;

  @GET
  public List<ApiInfoTreeDTO> findApiList() {
    return apiDesignApplicationService.findApiInfoTree();
  }

  @POST
  public ApiInfo create(ApiInfo apiInfo) {
    return apiDesignApplicationService.createApiInfo(apiInfo);
  }

  @PUT
  @Path("/{id}")
  public ApiInfo update(@PathParam("id") String id, ApiInfo apiInfo) {
    apiInfo.setId(id);
    return apiDesignApplicationService.updateApiInfo(apiInfo);
  }

  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("id") String id) {
    apiDesignApplicationService.deleteApiInfo(id);
  }

}
