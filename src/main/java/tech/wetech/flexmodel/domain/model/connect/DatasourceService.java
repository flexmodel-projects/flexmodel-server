package tech.wetech.flexmodel.domain.model.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceService {

  @Inject
  DatasourceRepository datasourceRepository;

  @Inject
  SessionDatasource sessionDatasource;

  public ValidateResult validate(Datasource datasource) {
    return sessionDatasource.validate(datasource);
  }

  public Datasource createDatasource(Datasource datasource) {
    datasource = datasourceRepository.save(datasource);
    sessionDatasource.add(datasource);
    return datasource;
  }

  public Datasource updateDatasource(Datasource datasource) {
    datasource = datasourceRepository.save(datasource);
    sessionDatasource.delete(datasource.getName());
    sessionDatasource.add(datasource);
    return datasource;
  }

  public List<Datasource> findAll() {
    return datasourceRepository.findAll();
  }

  public void deleteDatasource(String datasourceName) {
    datasourceRepository.delete(datasourceName);
    sessionDatasource.delete(datasourceName);
  }
}
