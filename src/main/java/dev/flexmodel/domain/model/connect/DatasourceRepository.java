package dev.flexmodel.domain.model.connect;

import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface DatasourceRepository {

  List<Datasource> findAll(String projectId);

  List<Datasource> find(String projectId, Predicate filter);

  Datasource save(Datasource datasource);

  void delete(String projectId, String name);

  Integer count(String projectId);
}
