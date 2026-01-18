package dev.flexmodel.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import dev.flexmodel.codegen.entity.ApiDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiDefinitionTreeDTO extends ApiDefinition {

  private List<ApiDefinitionTreeDTO> children = new ArrayList<>();

  public ApiDefinitionTreeDTO() {
  }

  public ApiDefinitionTreeDTO(ApiDefinition apiDefinition) {
    this.setId(apiDefinition.getId());
    this.setName(apiDefinition.getName());
    this.setParentId(apiDefinition.getParentId());
    this.setType(apiDefinition.getType());
    this.setMethod(apiDefinition.getMethod());
    this.setPath(apiDefinition.getPath());
    this.setMeta(apiDefinition.getMeta());
    this.setEnabled(apiDefinition.getEnabled());
    this.setCreatedBy(apiDefinition.getCreatedBy());
    this.setUpdatedBy(apiDefinition.getUpdatedBy());
    this.setCreatedAt(apiDefinition.getCreatedAt());
    this.setUpdatedAt(apiDefinition.getUpdatedAt());
  }

}
