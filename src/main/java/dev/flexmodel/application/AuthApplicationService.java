package dev.flexmodel.application;

import dev.flexmodel.application.dto.*;
import dev.flexmodel.codegen.entity.Resource;
import dev.flexmodel.codegen.entity.Role;
import dev.flexmodel.domain.model.auth.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class AuthApplicationService {

  @Inject
  UserService userService;
  @Inject
  RoleService roleService;
  @Inject
  ResourceService resourceService;


  public User login(String username, String password) {
    return userService.login(username, password);
  }

  public List<String> findPermissions(String userId) {
    User user = userService.findById(userId);
    List<String> roleIds = Arrays.stream(user.getRoleIds().split(","))
      .filter(roleId -> !roleId.isEmpty())
      .toList();
    return roleService.findPermissions(roleIds);
  }

  public User getUser(String userId) {
    return userService.getUser(userId);
  }

  public List<UserResponse> findAllUsers() {
    List<Role> allRoles = roleService.findAll();
    return userService.findAll().stream()
      .map(user -> UserResponse.fromUser(user, allRoles))
      .toList();
  }

  public UserResponse findUserById(String userId) {
    User user = userService.findById(userId);
    List<Role> allRoles = roleService.findAll();
    return user != null ? UserResponse.fromUser(user, allRoles) : null;
  }

  public UserResponse createUser(UserRequest request) {
    User user = new User();
    user.setId(request.getId());
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setCreatedBy(request.getCreatedBy());
    user.setUpdatedBy(request.getUpdatedBy());
    user.setRoleIds(String.join(",", request.getRoleIds()));

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
      try {
        user.setPasswordHash(SecurityUtil.md5(request.getId(), request.getPassword()));
      } catch (Exception e) {
        throw new RuntimeException("Failed to hash password", e);
      }
    }

    User savedUser = userService.save(user);
    List<Role> allRoles = roleService.findAll();
    return UserResponse.fromUser(savedUser, allRoles);
  }

  public UserResponse updateUser(UserRequest request) {
    User existingUser = userService.findById(request.getId());
    if (existingUser == null) {
      throw new RuntimeException("User not found");
    }

    User user = new User();
    user.setId(request.getId());
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setCreatedBy(existingUser.getCreatedBy());
    user.setUpdatedBy(request.getUpdatedBy());
    user.setCreatedAt(existingUser.getCreatedAt());
    user.setRoleIds(String.join(",", request.getRoleIds()));

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
      try {
        user.setPasswordHash(SecurityUtil.md5(request.getId(), request.getPassword()));
      } catch (Exception e) {
        throw new RuntimeException("Failed to hash password", e);
      }
    } else {
      user.setPasswordHash(existingUser.getPasswordHash());
    }

    User savedUser = userService.save(user);
    List<Role> allRoles = roleService.findAll();
    return UserResponse.fromUser(savedUser, allRoles);
  }

  public void deleteUser(String userId) {
    userService.delete(userId);
  }

  public List<RoleResponse> findAllRoles() {
    List<Resource> allResources = resourceService.findAll();
    return roleService.findAll().stream()
      .map(role -> RoleResponse.fromRole(role, allResources))
      .toList();
  }

  public RoleResponse findRoleById(String roleId) {
    Role role = roleService.findById(roleId);
    if (role == null) {
      return null;
    }
    List<Resource> allResources = resourceService.findAll();
    return RoleResponse.fromRole(role, allResources);
  }

  public RoleResponse createRole(RoleRequest request) {
    Role role = new Role();
    role.setId(request.getId());
    role.setName(request.getName());
    role.setDescription(request.getDescription());
    role.setResourceIds(String.join(",", request.getResourceIds()));

    Role savedRole = roleService.create(role);
    List<Resource> allResources = resourceService.findAll();
    return RoleResponse.fromRole(savedRole, allResources);
  }

  public RoleResponse updateRole(RoleRequest request) {
    Role existingRole = roleService.findById(request.getId());
    if (existingRole == null) {
      throw new RuntimeException("Role not found");
    }

    Role role = new Role();
    role.setId(request.getId());
    role.setName(request.getName());
    role.setDescription(request.getDescription());
    role.setResourceIds(String.join(",", request.getResourceIds()));
    role.setCreatedBy(existingRole.getCreatedBy());
    role.setCreatedAt(existingRole.getCreatedAt());

    Role savedRole = roleService.update(role);
    List<Resource> allResources = resourceService.findAll();
    return RoleResponse.fromRole(savedRole, allResources);
  }

  public void deleteRole(String roleId) {
    roleService.delete(roleId);
  }

  public List<ResourceResponse> findAllResources() {
    return resourceService.findAll().stream()
      .map(ResourceResponse::fromResource)
      .toList();
  }

  public List<ResourceTreeResponse> findResourceTree() {
    List<Resource> allResources = resourceService.findAll();
    List<ResourceTreeResponse> allTreeNodes = allResources.stream()
      .map(ResourceTreeResponse::fromResource)
      .toList();

    Map<Long, ResourceTreeResponse> nodeMap = allTreeNodes.stream()
      .collect(Collectors.toMap(ResourceTreeResponse::getId, node -> node));

    List<ResourceTreeResponse> rootNodes = new ArrayList<>();

    for (ResourceTreeResponse node : allTreeNodes) {
      Long parentId = node.getParentId();
      if (parentId == null || parentId == 0) {
        rootNodes.add(node);
      } else {
        ResourceTreeResponse parentNode = nodeMap.get(parentId);
        if (parentNode != null) {
          parentNode.addChild(node);
        }
      }
    }
    return rootNodes;
  }

}
