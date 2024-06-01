package tech.wetech.flexmodel.domain.model.connect;

import java.util.List;

/**
 * @author cjbi
 */
public interface DatasourceRepository {

  List<Datasource> findAll();

  Datasource save(Datasource datasource);

  void delete(String id);
}
