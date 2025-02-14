package tech.wetech.flexmodel.domain.model.connect;

import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.dsl.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface DatasourceRepository {

  List<Datasource> findAll();

  List<Datasource> find(Predicate filter);

  Datasource save(Datasource datasource);

  void delete(String id);
}
