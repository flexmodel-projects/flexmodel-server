package tech.wetech.flexmodel.domain.model.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Datasource;

import java.util.List;
import java.util.Optional;

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
    Optional<Datasource> optional = findOne(datasource.getName());
    if(optional.isPresent()) {
      throw new ConnectException("The data source name is duplicated");
    }
    datasource.setType("user");
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
    return datasourceRepository.find(f -> f.equalTo("enabled", true));
  }

  public Optional<Datasource> findOne(String datasourceName) {
    return datasourceRepository.find(f -> f.equalTo("name", datasourceName))
      .stream()
      .findFirst();
  }

  public void deleteDatasource(String datasourceName) {
    datasourceRepository.delete(datasourceName);
    sessionDatasource.delete(datasourceName);
  }
}
