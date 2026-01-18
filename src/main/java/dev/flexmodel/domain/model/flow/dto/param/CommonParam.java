package dev.flexmodel.domain.model.flow.dto.param;

public class CommonParam {
  private String projectId;
  private String caller;

  public CommonParam(String projectId, String caller) {
    this.projectId = projectId;
    this.caller = caller;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getCaller() {
    return caller;
  }

  public void setCaller(String caller) {
    this.caller = caller;
  }

  @Override
  public String toString() {
    return "CommonParam{" +
           "tenant='" + projectId + '\'' +
           ", caller='" + caller + '\'' +
           '}';
  }
}
