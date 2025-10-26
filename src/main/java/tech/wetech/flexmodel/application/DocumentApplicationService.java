package tech.wetech.flexmodel.application;

import graphql.language.*;
import graphql.parser.Parser;
import graphql.schema.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.enumeration.ApiType;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.domain.model.settings.SettingsService;
import tech.wetech.flexmodel.graphql.GraphQLProvider;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.ScalarType;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.shared.FlexmodelConfig;
import tech.wetech.flexmodel.shared.matchers.UriTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.codegen.StringUtils.*;

/**
 * @author cjbi
 */
@ApplicationScoped
@Slf4j
@SuppressWarnings("all")
public class DocumentApplicationService {

  @Inject
  ApiDefinitionService apiDefinitionService;

  @Inject
  ModelService modelService;

  @Inject
  GraphQLProvider graphQLProvider;

  @Inject
  SettingsService settingsService;

  @Inject
  FlexmodelConfig config;

  public static final Map<String, Map> TYPE_MAPPING = new HashMap<>();
  public static final String GRAPHQL_INTERNAL_DIRECTIVE = "internal";

  static {
    TYPE_MAPPING.put("ID", Map.of("type", "string"));
    TYPE_MAPPING.put("String", Map.of("type", "string"));
    TYPE_MAPPING.put("Int", Map.of("type", "integer", "format", "int32"));
    TYPE_MAPPING.put("Float", Map.of("type", "number", "format", "double"));
    TYPE_MAPPING.put("Boolean", Map.of("type", "boolean"));
    TYPE_MAPPING.put("JSON", Map.of("type", "object"));
  }

  public Map<String, Object> getOpenApi() {
    List<ApiDefinition> apis = apiDefinitionService.findAll();
    Map<String, Object> openAPI = new HashMap<>();
    openAPI.put("openapi", "3.0.3");
    openAPI.put("info", buildInfo());
    openAPI.put("components", buildComponents(apis));
    openAPI.put("servers", List.of(Map.of("url", config.apiRootPath())));
    openAPI.put("schemas", List.of("https", "http"));
    openAPI.put("tags", buildTags(apis));
    openAPI.put("paths", buildPaths(apis));
    return openAPI;
  }

  private Map<String, String> buildInfo() {
    return Map.of(
      "title", "Flexmodel API document",
      "description", """
        Interact with Flexmodel programmatically
        """
    );
  }

  private GraphQLFieldDefinition getGraphQLFieldDefinition(GraphQLSchema graphQLSchema, Field field) {
    GraphQLFieldDefinition fieldDefinition = graphQLSchema.getQueryType().getFieldDefinition(field.getName());
    if (fieldDefinition == null) {
      fieldDefinition = graphQLSchema.getMutationType().getFieldDefinition(field.getName());
    }
    return fieldDefinition;
  }

  private String getSanitizeName(ApiDefinition apiDefinition) {
    return capitalize(snakeToCamel(sanitize(apiDefinition.getMethod().toLowerCase() + "_" + apiDefinition.getPath())));
  }

  private Map<String, Object> buildSchemas(List<ApiDefinition> apis) {
    Map<String, Object> definitions = new HashMap<>();
    GraphQLSchema graphQLSchema = graphQLProvider.getGraphQL().getGraphQLSchema();
    for (ApiDefinition api : apis) {
      try {
        if (api.getType() != ApiType.API) {
          continue;
        }
        String sanitizeName = getSanitizeName(api);
        Map<String, Object> meta = (Map<String, Object>) api.getMeta();
        if (meta == null || meta.isEmpty()) {
          continue;
        }
        Map<String, Object> execution = (Map<String, Object>) meta.get("execution");
        String operationName = (String) execution.get("operationName");
        String query = (String) execution.get("query");
        Map<String, Object> variables = (Map<String, Object>) execution.get("variables");
        Map<String, Object> headers = (Map<String, Object>) execution.get("headers");

        Parser parser = new Parser();
        Document document = parser.parse(query);

        // 3. 提取变量
        List<VariableDefinition> variableDefinitions = getVariableDefinitions(document);

        Map<String, Object> requestType = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        requestType.put("type", "object");
        requestType.put("properties", properties);
        for (VariableDefinition variableDefinition : variableDefinitions) {
          String variableName = variableDefinition.getName();
          String variableType = variableDefinition.getType().toString();
          Map<String, Object> propertyMap = new HashMap<>();
          properties.put(variableName, TYPE_MAPPING.getOrDefault(variableType, Map.of("type", "string")));
        }
        definitions.put(sanitizeName + "Request", requestType);

        // 3. 提取返回参数
        List<Field> returnFields = document.getDefinitions().stream()
          .filter(def -> def instanceof OperationDefinition)
          .flatMap(def -> ((OperationDefinition) def).getSelectionSet().getSelections().stream())
          .filter(selection -> selection instanceof Field)
          .map(selection -> (Field) selection)
          .collect(Collectors.toList());

        // 4. 输出返回参数信息
        for (Field field : returnFields) {
          GraphQLFieldDefinition fieldDefinition = getGraphQLFieldDefinition(graphQLSchema, field);
          if (fieldDefinition == null) {
            continue;
          }
          GraphQLType originType = fieldDefinition.getType();
          boolean isList = false;
          if (originType instanceof GraphQLNonNull graphQLNonNull) {
            originType = graphQLNonNull.getOriginalWrappedType();
          }
          if (originType instanceof GraphQLList graphQLList) {
            isList = true;
            originType = graphQLList.getOriginalWrappedType();
          }
          if (originType instanceof GraphQLNonNull graphQLNonNull) {
            originType = graphQLNonNull.getOriginalWrappedType();
          }

          Map<String, Object> responseType = new HashMap<>();
          Map<String, Object> wrapperProperties = new HashMap<>();
          Map<String, Object> typeProperties = new HashMap<>();

          // 如果有子字段，可以进一步提取
          SelectionSet selectionSet = field.getSelectionSet();
          if (selectionSet != null) {
            List<Field> subFields = selectionSet.getSelections().stream()
              .filter(selection -> selection instanceof Field)
              .map(selection -> (Field) selection)
              .collect(Collectors.toList());
            for (Field subField : subFields) {
              if (originType instanceof GraphQLObjectType) {
                GraphQLObjectType objectType = (GraphQLObjectType) originType;
                GraphQLFieldDefinition subFieldDefinition = objectType.getFieldDefinition(subField.getName());
                if (subFieldDefinition != null) {
                  GraphQLOutputType definitionType = subFieldDefinition.getType();
                  if (definitionType instanceof GraphQLScalarType graphQLScalarType) {
                    typeProperties.put(subField.getName(), TYPE_MAPPING.get(graphQLScalarType.getName()));
                  } else {
                    log.error("Unkown definitionType: {}, sanitizeName={}", definitionType, sanitizeName);
                  }
                }
              }
            }
          }

          Map<String, Object> returnDataTypeMap = new HashMap<>();
          if (isList) {
            returnDataTypeMap.put("type", "array");
            returnDataTypeMap.put("items", Map.of("type", "object", "properties", typeProperties));
          } else {
            returnDataTypeMap.put("type", "object");
            returnDataTypeMap.put("properties", typeProperties);
          }
          wrapperProperties.put("data", returnDataTypeMap);
          responseType.put("type", "object");
          responseType.put("properties", wrapperProperties);
          definitions.put(sanitizeName + "Response", responseType);
        }
      } catch (Exception e) {
        log.error("Build api doc error: {}", e.getMessage(), e);
      }

    }
    return definitions;
  }

  /**
   * 提取变量
   *
   * @param document
   * @return
   */
  private List<VariableDefinition> getVariableDefinitions(Document document) {
    return document.getDefinitions().stream()
      .filter(def -> def instanceof OperationDefinition)
      .flatMap(def -> ((OperationDefinition) def).getVariableDefinitions().stream()
        .filter(f -> f.getDirectives(GRAPHQL_INTERNAL_DIRECTIVE).isEmpty())
      )
      .collect(Collectors.toList());
  }

  private String addModelDefinition(String datasourceName, String modelName, Map<String, Object> definitions) {
    Map<String, Object> typeMapping = new HashMap<>();
    typeMapping.put(ScalarType.STRING.getType(), Map.of("type", "string"));
    typeMapping.put(ScalarType.INT.getType(), Map.of("type", "integer", "format", "int32"));
    typeMapping.put(ScalarType.LONG.getType(), Map.of("type", "integer", "format", "int64"));
    typeMapping.put(ScalarType.FLOAT.getType(), Map.of("type", "number", "format", "double"));
    typeMapping.put(ScalarType.BOOLEAN.getType(), Map.of("type", "boolean"));
//    typeMapping.put("", Map.of("type", "array"));
    typeMapping.put(ScalarType.JSON.getType(), Map.of("type", "object"));
    EntityDefinition entity = (EntityDefinition) modelService.findModel(datasourceName, modelName).orElseThrow();
    if (entity == null) {
      return null;
    }
    Map<String, Object> object = new HashMap<>();
    String refModelName = datasourceName + "." + entity.getName();
    if (definitions.containsKey(refModelName)) {
      return "#/components/schemas/" + refModelName;
    }
    definitions.put(refModelName, object);
    object.put("type", "object");
    Map<String, Object> properties = new HashMap<>();
    object.put("properties", properties);
    for (TypedField<?, ?> field : entity.getFields()) {
      if (field instanceof RelationField relationField) {
        Map<String, Object> refProp = new HashMap<>();
        refProp.put("type", relationField.isMultiple() ? "array" : "object");
        refProp.put("items", Map.of("$ref", addModelDefinition(datasourceName, relationField.getFrom(), definitions)));
        properties.put(field.getName(), refProp);
        continue;
      }
      properties.put(field.getName(), typeMapping.getOrDefault(field.getType(), Map.of("type", "string")));
    }
    return "#/components/schemas/" + refModelName;
  }

  public Map<String, Object> buildComponents(List<ApiDefinition> apis) {
    Map<String, Object> components = new HashMap<>();
    components.put("securitySchemes", Map.of(
      "bearerAuth", Map.of(
        "type", "apiKey",
        "name", "Authorization",
        "in", "header"
      )
    ));
    components.put("schemas", buildSchemas(apis));
    return components;
  }

  public List<Map<String, Object>> buildTags(List<ApiDefinition> apis) {
    List<Map<String, Object>> tags = new ArrayList<>();
    for (ApiDefinition api : apis) {
      if (api.getType() == ApiType.FOLDER) {
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", api.getName());
        tag.put("description", api.getName());
        tags.add(tag);
      }
    }
    return tags;
  }

  public Map<String, Object> buildPaths(List<ApiDefinition> apis) {
    Map<String, Object> paths = new HashMap<>();
    for (ApiDefinition api : apis) {
      try {
        if (api.getType() != ApiType.API) {
          continue;
        }
        String sanitizeName = getSanitizeName(api);
        Map<String, Object> path = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        String tag = apis.stream()
          .filter(a -> a.getId().equals(api.getParentId()))
          .map(ApiDefinition::getName)
          .findFirst()
          .orElse(null);
        if (tag != null) {
          content.put("tags", List.of(tag));
        }
        Map<String, Object> meta = (Map<String, Object>) api.getMeta();
        if (meta == null || meta.isEmpty()) {
          continue;
        }
        String restAPIType = (String) meta.get("type");
        content.put("summary", api.getName());
        content.put("operationId", api.getId());

        Map<String, Object> responses = new HashMap<>();
        responses.put("200", buildResponse200(api));
        responses.put("400", Map.of("description", "invalid input"));
        responses.put("404", Map.of("description", "not found"));
        content.put("responses", responses);

        Map<String, Object> execution = (Map<String, Object>) meta.get("execution");
        String operationName = (String) execution.get("operationName");
        String query = (String) execution.get("query");
        Map<String, Object> variables = (Map<String, Object>) execution.get("variables");
        Map<String, Object> headers = (Map<String, Object>) execution.get("headers");

//        content.put("description",
//          String.format("""
//            ```graphql
//            %s
//            ```
//            """, query));

        UriTemplate uriTemplate = new UriTemplate(api.getPath());
        Map<String, String> pathParamters = uriTemplate.match(new UriTemplate(api.getPath()));

        Parser parser = new Parser();
        Document document = parser.parse(query);

        boolean supportsBody = !(api.getMethod().equals("GET") || api.getMethod().equals("DELETE"));
        content.put("parameters", buildParameters(api, document, supportsBody));
        if (supportsBody) {
          content.put("requestBody",
            Map.of(
              "required", true,
              "description", "json body",
              "content",
              Map.of("application/json",
                Map.of("schema",
                  Map.of("$ref", "#/components/schemas/" + sanitizeName + "Request")))));
        }

        // 接口是否鉴权
        boolean isAuth = (boolean) meta.get("auth");
        if (isAuth) {
          content.put("security", List.of(Map.of("bearerAuth", List.of())));
        }
        path.put(api.getMethod().toLowerCase(), content);
        if (paths.containsKey(api.getPath())) {
          Map<String, Object> existPath = (Map<String, Object>) paths.get(api.getPath());
          existPath.put(api.getMethod().toLowerCase(), content);
        } else {
          paths.put(api.getPath(), path);
        }
      } catch (Exception e) {
        log.error("Build api doc error: {}", e.getMessage(), e);
      }

    }
    // if graphql endpoint is enabled
    Settings settings = settingsService.getSettings();
    Map<String, Object> graphqlPath = new HashMap<>();
    Map<String, Object> typeProperties = new HashMap<>();
    typeProperties.put("query", Map.of("type", "string"));
    typeProperties.put("operationName", Map.of("type", "string"));
    typeProperties.put("variables", Map.of("type", "object"));
    graphqlPath.put("post", Map.of(
      "summary", "GraphQL Endpoint",
      "operationId", "GraphQL Endpoint",
      "responses", Map.of("200", Map.of("description", "GraphQL response")),
      "requestBody", Map.of(
        "required", true,
        "content",
        Map.of("application/json",
          Map.of("schema", Map.of("type", "object", "properties", typeProperties))))
    ));
    paths.put(settings.getSecurity().getGraphqlEndpointPath(), graphqlPath);


    return paths;
  }

  private List<Map<String, Object>> buildParameters(ApiDefinition apiDefinition, Document document, boolean supportsBody) {
    // 遍历每一个操作定义，提取其中的变量定义
    List<Map<String, Object>> parameters = new ArrayList<>();

    Set<String> pathNames = new UriTemplate(apiDefinition.getPath()).match(new UriTemplate(apiDefinition.getPath())).keySet();

    for (String pathName : pathNames) {
      parameters.add(
        Map.of(
          "name", pathName,
          "in", "path",
          "required", true,
          "schema", Map.of("type", "string")
        )
      );
    }

    if (!supportsBody) {
      // 3. 提取变量
      List<VariableDefinition> variableDefinitions = getVariableDefinitions(document);

      // 4. 输出变量信息
      for (VariableDefinition variableDefinition : variableDefinitions) {
        String variableName = variableDefinition.getName();
        String variableType = variableDefinition.getType().toString();
        if (pathNames.contains(variableName)) {
          continue;
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("name", variableDefinition.getName());
        parameter.put("in", "query");
        parameter.put("description", "");
        parameter.put("required", variableDefinition.getType() instanceof NonNullType);
        if (variableDefinition.getType() instanceof NonNullType nonNullType) {
          String typeName = ((TypeName) nonNullType.getType()).getName();
          Map type = TYPE_MAPPING.getOrDefault(typeName, Map.of("type", "string"));
          parameter.putAll(type);
        } else {
          String typeName = ((TypeName) variableDefinition.getType()).getName();
          Map type = TYPE_MAPPING.getOrDefault(typeName, Map.of("type", "string"));
          parameter.putAll(type);
        }
        parameters.add(parameter);
      }
    }

    return parameters;
  }

  private Map<String, Object> buildResponse200(ApiDefinition api) {
    String sanitizeName = getSanitizeName(api);
    return Map.of(
      "description", "successful operation",
      "content", Map.of(
        "application/json",
        Map.of("schema",
          Map.of("$ref", "#/components/schemas/" + sanitizeName + "Response")))
    );
  }

}
