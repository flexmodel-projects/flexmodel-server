package dev.flexmodel.application.dto;

import lombok.Getter;
import dev.flexmodel.codegen.entity.Role;
import lombok.Setter;
import dev.flexmodel.codegen.entity.Resource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class RoleResponse {

  private String id;
  private String name;
  private String description;
  private String createdBy;
  private String updatedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<RoleResource> resources;

  public static RoleResponse fromRole(Role role) {
    return fromRole(role, null);
  }

  public static RoleResponse fromRole(Role role, List<Resource> allResources) {
    RoleResponse response = new RoleResponse();
    response.setId(role.getId());
    response.setName(role.getName());
    response.setDescription(role.getDescription());
    response.setCreatedBy(role.getCreatedBy());
    response.setUpdatedBy(role.getUpdatedBy());
    response.setCreatedAt(role.getCreatedAt());
    response.setUpdatedAt(role.getUpdatedAt());

    if (allResources != null && role.getResourceIds() != null && !role.getResourceIds().isEmpty()) {
      List<String> resourceIdList = Arrays.asList(role.getResourceIds().split(","));
      List<RoleResource> roleResources = allResources.stream()
        .filter(resource -> resourceIdList.contains(String.valueOf(resource.getId())))
        .map(resource -> {
          RoleResource roleResource = new RoleResource();
          roleResource.setId(resource.getId());
          roleResource.setName(resource.getName());
          roleResource.setPermission(resource.getPermission());
          return roleResource;
        })
        .collect(Collectors.toList());
      response.setResources(roleResources);
    }

    return response;
  }

  @Setter
  @Getter
  public static class RoleResource {
    private Long id;
    private String name;
    private String permission;
  }

}
