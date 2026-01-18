package dev.flexmodel.domain.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author cjbi
 */
@Getter
@Setter
@ToString
public class ApiDefinitionMeta {
  private boolean auth;
  private String identityProvider;
  private boolean rateLimitingEnabled;
  private int maxRequestCount;
  private int intervalInSeconds;
  private Execution execution;
  private Document document;

  @Getter
  @Setter
  public static class Execution {
    private String preScript;
    private String postScript;
    private String executionType = "graphql";
    private String executionScript;
    private String query;
    private Map<String, Object> variables;
    private String operationName;
    private Map<String, Object> headers;
  }

  @Getter
  @Setter
  public static class Document {
    private DocumentIO input;
    private DocumentIO output;
  }

  @Getter
  @Setter
  public static class DocumentIO {
    /**
     * json schema 格式的数据
     */
    private Map<String, Object> schema;
  }

}
