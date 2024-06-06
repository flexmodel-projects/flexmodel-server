package tech.wetech.flexmodel.domain.model.apidesign;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiInfoRepository {

  void deleteByParentId(String parentId);

  List<ApiInfo> findAll();

  ApiInfo save(ApiInfo record);

  void delete(String id);

}
