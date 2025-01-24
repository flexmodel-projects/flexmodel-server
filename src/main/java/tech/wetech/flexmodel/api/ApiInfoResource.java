package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.ApiDesignApplicationService;
import tech.wetech.flexmodel.application.dto.ApiInfoTreeDTO;
import tech.wetech.flexmodel.application.dto.GenerateAPIsDTO;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Path(BASE_PATH + "/apis")
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
  public ApiInfo create(@Valid ApiInfo apiInfo) {
    return apiDesignApplicationService.createApiInfo(apiInfo);
  }

  @PUT
  @Path("/{id}")
  public ApiInfo update(@PathParam("id") String id, @Valid ApiInfo apiInfo) {
    apiInfo.setId(id);
    return apiDesignApplicationService.updateApiInfo(apiInfo);
  }

  @PATCH
  @Path("/{id}")
  public ApiInfo updateIgnoreNull(@PathParam("id") String id, @Valid ApiInfo apiInfo) {
    apiInfo.setId(id);
    return apiDesignApplicationService.updateApiInfoIgnoreNull(apiInfo);
  }

  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("id") String id) {
    apiDesignApplicationService.deleteApiInfo(id);
  }

  @POST
  @Path("/generate")
  public void generateAPIs(@Valid GenerateAPIsDTO dto) {
    apiDesignApplicationService.generateAPIs(dto);
  }

}
