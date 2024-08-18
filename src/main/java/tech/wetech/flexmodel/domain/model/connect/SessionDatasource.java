package tech.wetech.flexmodel.domain.model.connect;

import tech.wetech.flexmodel.codegen.entity.Datasource;

/**
 * @author cjbi
 */
public interface SessionDatasource {

  ValidateResult validate(Datasource datasource);

  void add(Datasource datasource);

  void delete(String datasourceName);

}
