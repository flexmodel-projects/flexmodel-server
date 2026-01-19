package dev.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Role;

import java.util.List;

@ApplicationScoped
public class RoleService {

  @Inject
  RoleRepository roleRepository;

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
}
