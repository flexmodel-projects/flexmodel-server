package dev.flexmodel.shared.matchers;


import java.util.*;

/**
 * Extracts path parameters from URIs used to create web socket connections using the URI template defined for the
 * associated Endpoint.
 */
public class UriTemplate {

  private final String normalized;
  private final List<Segment> segments = new ArrayList<>();
  private final boolean hasParameters;

  public UriTemplate(String path) throws DeploymentException {

    if (path == null || path.length() == 0 || !path.startsWith("/") || path.contains("/../") ||
        path.contains("/./") || path.contains("//")) {
      throw new DeploymentException("Invalid path: " + path);
    }

    StringBuilder normalized = new StringBuilder(path.length());
    Set<String> paramNames = new HashSet<>();

    // Include empty segments.
    String[] segments = path.split("/", -1);
    int paramCount = 0;
    int segmentCount = 0;

    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];
      if (segment.length() == 0) {
        if (i == 0 || (i == segments.length - 1 && paramCount == 0)) {
          // Ignore the first empty segment as the path must always
          // start with '/'
          // Ending with a '/' is also OK for instances used for
          // matches but not for parameterised templates.
          continue;
        } else {
          // As per EG discussion, all other empty segments are
          // invalid
          throw new DeploymentException("Empty segment: " + path);
        }
      }
      normalized.append('/');
      int index = -1;
      if (segment.startsWith("{") && segment.endsWith("}")) {
        index = segmentCount;
        segment = segment.substring(1, segment.length() - 1);
        normalized.append('{');
        normalized.append(paramCount++);
        normalized.append('}');
        if (!paramNames.add(segment)) {
          throw new DeploymentException("Duplicate parameter: " + segment);
        }
      } else {
        if (segment.contains("{") || segment.contains("}")) {
          throw new DeploymentException("Invalid segment: " + segment);
        }
        normalized.append(segment);
      }
      this.segments.add(new Segment(index, segment));
      segmentCount++;
    }

    this.normalized = normalized.toString();
    this.hasParameters = paramCount > 0;
  }


  public Map<String, String> match(UriTemplate candidate) {

    Map<String, String> result = new HashMap<>();

    List<Segment> candidateSegments = candidate.getSegments();
    int candidateIndex = 0;

    for (Segment targetSegment : segments) {
      if (candidateIndex >= candidateSegments.size()) {
        return null;
      }

      if (targetSegment.getParameterIndex() == -1) {
        if (".*".equals(targetSegment.getValue())) {
          return result;
        }
        if (!targetSegment.getValue().equals(candidateSegments.get(candidateIndex).getValue())) {
          return null;
        }
        candidateIndex++;
      } else {
        result.put(targetSegment.getValue(), candidateSegments.get(candidateIndex).getValue());
        candidateIndex++;
      }
    }

    if (candidateIndex != candidateSegments.size()) {
      return null;
    }

    return result;
  }


  public boolean hasParameters() {
    return hasParameters;
  }


  public int getSegmentCount() {
    return segments.size();
  }


  public String getNormalizedPath() {
    return normalized;
  }


  private List<Segment> getSegments() {
    return segments;
  }


  private static class Segment {
    private final int parameterIndex;
    private final String value;

    Segment(int parameterIndex, String value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }


    public int getParameterIndex() {
      return parameterIndex;
    }


    public String getValue() {
      return value;
    }
  }

  public static class DeploymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DeploymentException(String message) {
      super(message);
    }

    public DeploymentException(String message, Throwable cause) {
      super(message, cause);
    }
  }


}
