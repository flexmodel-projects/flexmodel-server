package dev.flexmodel.domain.model.auth;

import dev.flexmodel.codegen.entity.Role;

import java.util.List;

public interface RoleRepository {

  Role findById(String roleId);

  List<Role> findAll();

  Role save(Role role);

  void delete(String roleId);

  List<Role> findByIds(List<String> roleIds);

}
