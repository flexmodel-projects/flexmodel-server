package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.DatasourceDAO;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceRepository;
import tech.wetech.flexmodel.dsl.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceFmRepository implements DatasourceRepository {

  @Inject
  DatasourceDAO datasourceDAO;

  @Override
  public List<Datasource> findAll() {
    return datasourceDAO.findAll();
  }

  @Override
  public List<Datasource> find(Predicate filter) {
    return datasourceDAO.find(filter);
  }

  @Override
  public Datasource save(Datasource record) {
    return datasourceDAO.save(record);
  }

  @Override
  public void delete(String id) {
    datasourceDAO.deleteById(id);
  }
}
