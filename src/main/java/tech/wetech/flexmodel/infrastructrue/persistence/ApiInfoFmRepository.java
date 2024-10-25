package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.ApiInfoDAO;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoRepository;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiInfoFmRepository implements ApiInfoRepository {

  @Inject
  ApiInfoDAO apiInfoDAO;

  @Override
  public void deleteByParentId(String parentId) {
    apiInfoDAO.delete(f -> f.equalTo("parentId", parentId));
  }

  @Override
  public List<ApiInfo> findAll() {
    return apiInfoDAO.findAll();
  }

  @Override
  public ApiInfo save(ApiInfo record) {
    return apiInfoDAO.save(record);
  }

  @Override
  public void delete(String id) {
    apiInfoDAO.deleteById(id);
  }

  @Override
  public void updateIgnoreNull(String id, ApiInfo apiInfo) {
    apiInfoDAO.updateIgnoreNullById(apiInfo, id);
  }

}
