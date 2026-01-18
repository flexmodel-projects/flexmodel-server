package dev.flexmodel.interfaces.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.domain.model.BusinessException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

  @Override
  public Response toResponse(BusinessException e) {
    log.error("Handle exception, message={}", e.getMessage(), e);
    return getDefaultResponse(e);
  }

  public static Response getDefaultResponse(BusinessException e) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", 400);
    body.put("message", e.getMessage());
    body.put("success", false);
    return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
  }

}
