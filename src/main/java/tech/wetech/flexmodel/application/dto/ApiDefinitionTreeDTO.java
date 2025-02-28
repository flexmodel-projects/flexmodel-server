package tech.wetech.flexmodel.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;

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
    this.setCreatedAt(apiDefinition.getCreatedAt());
    this.setEnabled(apiDefinition.getEnabled());
    this.setUpdatedAt(apiDefinition.getUpdatedAt());
  }

}
