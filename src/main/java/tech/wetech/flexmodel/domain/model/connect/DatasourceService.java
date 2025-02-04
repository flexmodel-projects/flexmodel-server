package tech.wetech.flexmodel.domain.model.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.codegen.enumeration.DatasourceType;

import java.util.List;
import java.util.Map;
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

  public List<String> getPhysicsModelNames(Datasource datasource) {
    return sessionDatasource.getPhysicsModelNames(datasource);
  }

  public Datasource createDatasource(Datasource datasource) {
    Optional<Datasource> optional = findOne(datasource.getName());
    if (optional.isPresent()) {
      throw new ConnectException("The data source name is duplicated");
    }
    datasource.setType(DatasourceType.user);
    datasource = datasourceRepository.save(datasource);
    sessionDatasource.add(datasource);
    return datasource;
  }

  public Datasource updateDatasource(Datasource datasource) {
    Optional<Datasource> optional = findOne(datasource.getName());
    if (optional.isEmpty()) {
      return datasource;
    }
    datasource.setEnabled(optional.orElseThrow().getEnabled());
    datasource.setType(optional.orElseThrow().getType());
    datasource.setCreatedAt(optional.orElseThrow().getCreatedAt());
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

  public NativeQueryResult executeNativeQuery(String datasourceName, String statement, Map<String, Object> parameters) {
    return sessionDatasource.executeNativeQuery(datasourceName, statement, parameters);
  }
}
