package dev.flexmodel.domain.model.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.codegen.enumeration.DatasourceType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.flexmodel.query.Expressions.field;

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

  public Datasource createDatasource(String projectId, Datasource datasource) {
    Optional<Datasource> optional = findOne(projectId, datasource.getName());
    if (optional.isPresent()) {
      throw new ConnectException("The data source name is duplicated");
    }
    datasource.setType(DatasourceType.USER);
    datasource = datasourceRepository.save(datasource);
    sessionDatasource.add(datasource);
    return datasource;
  }

  public Datasource updateDatasource(String projectId, Datasource datasource) {
    Optional<Datasource> optional = findOne(projectId, datasource.getName());
    if (optional.isEmpty()) {
      return datasource;
    }
    datasource.setEnabled(optional.orElseThrow().getEnabled());
    datasource.setType(optional.orElseThrow().getType());
    datasource.setCreatedAt(optional.orElseThrow().getCreatedAt());
    datasource = datasourceRepository.save(datasource);
    sessionDatasource.delete(projectId, datasource.getName());
    sessionDatasource.add(datasource);
    return datasource;
  }

  public List<Datasource> findAll(String projectId) {
    return datasourceRepository.find(projectId, field(Datasource::getEnabled).eq(true));
  }

  public Optional<Datasource> findOne(String projectId, String datasourceName) {
    return datasourceRepository.find(projectId, field(Datasource::getName).eq(datasourceName))
      .stream()
      .findFirst();
  }

  public void deleteDatasource(String projectId, String datasourceName) {
    datasourceRepository.delete(projectId, datasourceName);
  }

  public NativeQueryResult executeNativeQuery(String projectId, String datasourceName, String statement, Map<String, Object> parameters) {
    return sessionDatasource.executeNativeQuery(projectId, datasourceName, statement, parameters);
  }

  public Integer count(String projectId) {
    return datasourceRepository.count(projectId);
  }
}
