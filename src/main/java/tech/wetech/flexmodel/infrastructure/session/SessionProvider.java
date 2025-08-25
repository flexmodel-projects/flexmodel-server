package tech.wetech.flexmodel.infrastructure.session;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionManager;

/**
 * @author cjbi
 */
@Slf4j
@RequestScoped
public class SessionProvider {

  @Inject
  SessionManager sessionManager;

  /**
   * 提供默认Session
   *
   * @return Session实例
   */
  @Produces
  @RequestScoped
  public Session provideSession() {
    log.debug("Providing default session");
    return sessionManager.getCurrentSession();
  }

  @PreDestroy
  public void destroy() {
    sessionManager.closeAllSessions();;
  }

}
