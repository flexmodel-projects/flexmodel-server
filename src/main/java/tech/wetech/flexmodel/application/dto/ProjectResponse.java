package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import tech.wetech.flexmodel.codegen.entity.Project;

/**
 * @author cjbi
 */
@Getter
public class ProjectResponse extends Project {

  private Integer apiCount;
  private Integer datasourceCount;
  private Integer flowCount;
  private Integer storageCount;

  public static ProjectResponse fromProject(Project project) {
    ProjectResponse response = new ProjectResponse();
    response.setId(project.getId());
    response.setName(project.getName());
    response.setDescription(project.getDescription());
    response.setEnabled(project.getEnabled());
    response.setOwnerId(project.getOwnerId());
    response.setCreatedBy(project.getCreatedBy());
    response.setUpdatedBy(project.getUpdatedBy());
    response.setCreatedAt(project.getCreatedAt());
    return response;
  }

  public ProjectResponse setApiCount(Integer apiCount) {
    this.apiCount = apiCount;
    return this;
  }

  public ProjectResponse setDatasourceCount(Integer datasourceCount) {
    this.datasourceCount = datasourceCount;
    return this;
  }

  public ProjectResponse setFlowCount(Integer flowCount) {
    this.flowCount = flowCount;
    return this;
  }

  public ProjectResponse setStorageCount(Integer storageCount) {
    this.storageCount = storageCount;
    return this;
  }
}
