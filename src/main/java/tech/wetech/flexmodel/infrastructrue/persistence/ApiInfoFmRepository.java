package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfo;
import tech.wetech.flexmodel.domain.model.apidesign.ApiInfoRepository;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiInfoFmRepository extends BaseFmRepository<ApiInfo, String> implements ApiInfoRepository {

  @Override
  public void deleteByParentId(String parentId) {
    session.delete(getEntityName(), String.format("""
      {
        "==": [{ "var": ["parentId"] }, "%s"]
      }
      """, parentId));
  }
}
