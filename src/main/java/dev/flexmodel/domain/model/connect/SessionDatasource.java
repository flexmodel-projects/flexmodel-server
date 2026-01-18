package dev.flexmodel.domain.model.connect;

import dev.flexmodel.codegen.entity.Datasource;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface SessionDatasource {

  List<String> getPhysicsModelNames(Datasource datasource);

  ValidateResult validate(Datasource datasource);

  void add(Datasource datasource);

  void delete(String projectId, String datasourceName);

  NativeQueryResult executeNativeQuery(String projectId, String datasourceName, String statement, Map<String, Object> parameters);

}
