package tech.wetech.flexmodel.domain.model.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class DatasourceService {

  @Inject
  DatasourceRepository datasourceRepository;

  public Datasource createDatasource(Datasource datasource) {
    return datasourceRepository.save(datasource);
  }

  public Datasource updateDatasource(Datasource datasource) {
    return datasourceRepository.save(datasource);
  }

  public List<Datasource> findAll() {
    return datasourceRepository.findAll();
  }

  public void deleteDatasource(Long id) {
    datasourceRepository.delete(id);
  }
}
