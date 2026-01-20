package dev.flexmodel.domain.model.auth;

import dev.flexmodel.codegen.entity.Resource;

import java.util.List;

public interface ResourceRepository {

  Resource findById(Long resourceId);

  List<Resource> findAll();

  Resource save(Resource resource);

  void delete(Long resourceId);

  List<String> findPermissions(List<Long> resourceIds);
}
