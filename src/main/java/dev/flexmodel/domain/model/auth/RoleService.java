package dev.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class RoleService {

  @Inject
  RoleRepository roleRepository;
  @Inject
  ResourceRepository resourceRepository;

  public List<Role> findAll() {
    return roleRepository.findAll();
  }

  public Role findById(String roleId) {
    return roleRepository.findById(roleId);
  }

  public Role create(Role role) {
    return roleRepository.save(role);
  }

  public Role update(Role role) {
    Role existingRole = roleRepository.findById(role.getId());
    if (existingRole == null) {
      throw new RuntimeException("Role not found");
    }
    role.setCreatedAt(existingRole.getCreatedAt());
    return roleRepository.save(role);
  }

  public void delete(String roleId) {
    roleRepository.delete(roleId);
  }

  public List<String> findPermissions(List<String> roleIds) {
    List<Role> roles = roleRepository.findByIds(roleIds);
    List<Long> resourceIds = new ArrayList<>();
    for (Role role : roles) {
      resourceIds.addAll(
        Arrays.stream(role.getResourceIds().split(","))
          .filter(resourceId -> !resourceId.isEmpty())
          .map(Long::parseLong)
          .toList()
      );
    }
    return resourceRepository.findPermissions(resourceIds);
  }

}
