package dev.flexmodel.interfaces.rest.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.flexmodel.interfaces.rest.exception.DefaultExceptionMapper.getDefaultResponse;

/**
 * @author cjbi
 */
@Provider
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
  @Override
  public Response toResponse(ValidationException e) {
    log.warn("Handle warn, message={}", e.getMessage());
    if (e instanceof ConstraintViolationException cvEx) {
      Map<String, String> errors = new HashMap<>();
      cvEx.getConstraintViolations().forEach(error -> {
        String property = error.getPropertyPath().toString();
        String errorMessage = error.getMessage();
        errors.put(property, errorMessage);
      });
      Map<String, Object> body = getErrorsMap(errors);
      return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
    return getDefaultResponse(e);
  }

  private static Map<String, Object> getErrorsMap(Map<String, String> fieldErrors) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", -1);
    body.put("message", fieldErrors.entrySet().stream()
      .map(m -> m.getKey() + " " + m.getValue())
      .collect(Collectors.joining(", "))
    );

    body.put("errors", fieldErrors);
    body.put("success", false);
    return body;
  }
}
