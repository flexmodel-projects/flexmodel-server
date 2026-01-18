package dev.flexmodel.shared;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
public class SessionContextHolder {

  private static final ThreadLocal<SessionContext> CONTEXT_HOLDER = ThreadLocal.withInitial(SessionContext::new);

  public static void setProjectId(String projectId) {
    CONTEXT_HOLDER.get().setProjectId(projectId);
  }

  public static String getProjectId() {
    return CONTEXT_HOLDER.get().getProjectId();
  }

  public static void setUserId(String userId) {
    CONTEXT_HOLDER.get().setUserId(userId);
  }

  public static String getUserId() {
    return CONTEXT_HOLDER.get().getUserId();
  }


  @Getter
  @Setter
  static class SessionContext {
    private String projectId;
    private String userId;
  }

}
