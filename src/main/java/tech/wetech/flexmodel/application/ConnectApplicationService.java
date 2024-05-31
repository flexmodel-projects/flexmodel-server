package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ConnectApplicationService {

  @Inject
  DatasourceService datasourceService;

  public List<Datasource> findDatasourceList() {
    return datasourceService.findAll();
  }

  public Datasource createDatasource(Datasource datasource) {
    return datasourceService.createDatasource(datasource);
  }

  public Datasource updateDatasource(String id, Datasource record) {
    record.setId(id);
    return datasourceService.updateDatasource(record);
  }

  public void deleteDatasource(Long id) {
    datasourceService.deleteDatasource(id);
  }

}
