package tech.wetech.flexmodel.domain.model.connect;

/**
 * @author cjbi
 */
public interface SessionDatasource {

  ValidateResult validate(Datasource datasource);

  void add(Datasource datasource);

  void delete(String datasourceName);

}
