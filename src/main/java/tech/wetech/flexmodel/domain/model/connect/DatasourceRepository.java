package tech.wetech.flexmodel.domain.model.connect;

import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.criterion.Example;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public interface DatasourceRepository {

  List<Datasource> findAll();

  List<Datasource> find(UnaryOperator<Example.Criteria> filter);

  Datasource save(Datasource datasource);

  void delete(String id);
}
