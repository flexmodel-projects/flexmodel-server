package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ModelingApplicationService {

  @Inject
  DatasourceService datasourceService;

  @Inject
  ModelService modelService;

  public List<Datasource> findDatasourceList() {
    return datasourceService.findAll();
  }

  public Datasource createDatasource(Datasource datasource) {
    return datasourceService.createDatasource(datasource);
  }

  public Datasource updateDatasource(Datasource record) {
    return datasourceService.updateDatasource(record);
  }

  public void deleteDatasource(String datasourceName) {
    datasourceService.deleteDatasource(datasourceName);
  }

  public List<Model> findModels(String datasourceName) {
    return modelService.findModels(datasourceName);
  }
}
