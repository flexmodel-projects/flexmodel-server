package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.codegen.entity.Project;

/**
 * @author cjbi
 */
@Setter
@Getter
public class ProjectResponse extends Project {

  private ProjectStats stats;
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

  public record ProjectStats(Integer apiCount, Integer datasourceCount, Integer flowCount, Integer storageCount) {
  }

}
