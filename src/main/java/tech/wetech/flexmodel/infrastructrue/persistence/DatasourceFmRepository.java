package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.criterion.Example;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceRepository;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceFmRepository extends BaseFmRepository<Datasource, String> implements DatasourceRepository {

  @Override
  public List<Datasource> findAll() {
    return super.findAll();
  }

  @Override
  public List<Datasource> find(UnaryOperator<Example.Criteria> filter) {
    return super.find(filter, null, null, null);
  }

  @Override
  public Datasource save(Datasource record) {
    return super.save(record);
  }

  @Override
  public void delete(String id) {
    super.delete(id);
  }
}
