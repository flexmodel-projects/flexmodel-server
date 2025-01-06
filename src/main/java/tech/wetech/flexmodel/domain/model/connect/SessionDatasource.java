package tech.wetech.flexmodel.domain.model.connect;

import tech.wetech.flexmodel.codegen.entity.Datasource;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface SessionDatasource {

  List<String> getPhysicsModelNames(Datasource datasource);

  ValidateResult validate(Datasource datasource);

  void add(Datasource datasource);

  void delete(String datasourceName);

  NativeQueryResult executeNativeQuery(String datasourceName, String statement, Map<String, Object> parameters);

}
