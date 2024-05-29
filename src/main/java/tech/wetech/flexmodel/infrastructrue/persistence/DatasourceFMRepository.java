package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.domain.model.connect.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceRepository;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceFMRepository extends BaseFMRepository<Datasource> implements DatasourceRepository {

  @Override
  public List<Datasource> findAll() {
    return super.findAll();
  }

  @Override
  public Datasource save(Datasource record) {
    return super.save(record);
  }

  @Override
  public void delete(Long id) {
    super.delete(id);
  }
}
