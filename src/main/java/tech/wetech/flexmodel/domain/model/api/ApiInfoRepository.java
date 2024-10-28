package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiInfo;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiInfoRepository {

  void deleteByParentId(String parentId);

  ApiInfo findById(String id);

  List<ApiInfo> findAll();

  ApiInfo save(ApiInfo record);

  void delete(String id);

  void updateIgnoreNull(String id, ApiInfo apiInfo);

}
