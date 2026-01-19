package dev.flexmodel.application.dto;

import lombok.Getter;
import dev.flexmodel.codegen.entity.Resource;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ResourceResponse {

  private Long id;
  private String name;
  private String permission;
  private String type;
  private Long parentId;

  public static ResourceResponse fromResource(Resource resource) {
    ResourceResponse response = new ResourceResponse();
    response.setId(resource.getId());
    response.setName(resource.getName());
    response.setPermission(resource.getPermission());
    response.setType(resource.getType());
    response.setParentId(resource.getParentId());
    return response;
  }

}
