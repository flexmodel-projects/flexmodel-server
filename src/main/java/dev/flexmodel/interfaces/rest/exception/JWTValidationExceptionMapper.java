package dev.flexmodel.interfaces.rest.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class JWTValidationExceptionMapper implements ExceptionMapper<JWTVerificationException> {

  @Override
  public Response toResponse(JWTVerificationException e) {
    return getDefaultResponse(e);
  }

  public static Response getDefaultResponse(JWTVerificationException e) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", 401);
    body.put("message", e.getMessage());
    body.put("success", false);
    return Response.status(Response.Status.UNAUTHORIZED).entity(body).build();
  }

}
