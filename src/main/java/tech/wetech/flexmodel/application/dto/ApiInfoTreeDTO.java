package tech.wetech.flexmodel.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiInfoTreeDTO extends ApiInfo {

  private List<ApiInfoTreeDTO> children = new ArrayList<>();

  public ApiInfoTreeDTO() {
  }

  public ApiInfoTreeDTO(ApiInfo apiInfo) {
    this.setId(apiInfo.getId());
    this.setName(apiInfo.getName());
    this.setParentId(apiInfo.getParentId());
    this.setType(apiInfo.getType());
    this.setMethod(apiInfo.getMethod());
    this.setPath(apiInfo.getPath());
    this.setMeta(apiInfo.getMeta());
    this.setCreatedAt(apiInfo.getCreatedAt());
    this.setEnabled(apiInfo.getEnabled());
    this.setUpdatedAt(apiInfo.getUpdatedAt());
  }

}
