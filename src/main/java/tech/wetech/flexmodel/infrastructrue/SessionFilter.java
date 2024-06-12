package tech.wetech.flexmodel.infrastructrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import tech.wetech.flexmodel.SessionFactory;

import java.io.IOException;

/**
 * @author cjbi
 */
@ApplicationScoped
@Provider
public class SessionFilter implements ContainerResponseFilter {

  @Inject
  SessionFactory sessionFactory;

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    try {
      sessionFactory.getCurrentSessionContext().destroy();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
