package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.application.ConnectApplicationService;
import tech.wetech.flexmodel.domain.model.connect.Datasource;

import java.util.List;

/**
 * @author cjbi
 */
@Path("/api/datasource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasourceResource {

  @Inject
  ConnectApplicationService connectApplicationService;

  @GET
  @Path("/list")
  public List<Datasource> findAll() {
    return connectApplicationService.findDatasourceList();
  }

  @POST
  public Datasource createDatasource(Datasource datasource) {
    return connectApplicationService.createDatasource(datasource);
  }

  @PUT
  @Path("/{id}")
  public Datasource updateDatasource(@PathParam("id") String id, Datasource datasource) {
    datasource.setId(id);
    return connectApplicationService.updateDatasource(id, datasource);
  }

  @DELETE
  @Path("/{id}")
  public void deleteDatasource(@PathParam("id") Long id) {
    connectApplicationService.deleteDatasource(id);
  }


}
