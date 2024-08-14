package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.domain.model.api.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoRepository;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiInfoFmRepository extends BaseFmRepository<ApiInfo, String> implements ApiInfoRepository {

  @Override
  public void deleteByParentId(String parentId) {
    withSession(session ->
      session.delete(getEntityName(), f -> f.equalTo("parentId", parentId)));
  }
}
