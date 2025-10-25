package tech.wetech.flexmodel.shared;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjbi
 */
public class SessionContextHolder {

  private static final ThreadLocal<SessionContext> CONTEXT_HOLDER = ThreadLocal.withInitial(SessionContext::new);

  public static void setTenantId(String tenantId) {
    CONTEXT_HOLDER.get().setTenantId(tenantId);
  }

  public static String getTenantId() {
    return CONTEXT_HOLDER.get().getTenantId();
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
    private String tenantId;
    private String userId;
  }

}
