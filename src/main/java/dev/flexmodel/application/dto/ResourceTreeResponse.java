package dev.flexmodel.application.dto;

import lombok.Getter;
import dev.flexmodel.codegen.entity.Resource;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ResourceTreeResponse {

  private Long id;
  private String name;
  private String permission;
  private String type;
  private Long parentId;
  private List<ResourceTreeResponse> children;

  public static ResourceTreeResponse fromResource(Resource resource) {
    ResourceTreeResponse response = new ResourceTreeResponse();
    response.setId(resource.getId());
    response.setName(resource.getName());
    response.setPermission(resource.getPermission());
    response.setType(resource.getType());
    response.setParentId(resource.getParentId());
    response.setChildren(new ArrayList<>());
    return response;
  }

  public void addChild(ResourceTreeResponse child) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    this.children.add(child);
  }
}
