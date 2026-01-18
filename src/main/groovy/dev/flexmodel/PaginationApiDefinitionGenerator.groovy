package dev.flexmodel

import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition
import dev.flexmodel.codegen.enumeration.ApiType

/**
 * @author cjbi
 */
class PaginationApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getName()
    out.println "query MyPaginationQuery( \$where: ${schemaName}_${modelName}_bool_exp, \$page: Int = 1, \$size: Int = 10) {"
    out.println "  list: ${schemaName}_list_${modelName}(where: \$where, page: \$page, size: \$size) {"
    context.getModelClass().getAllFields().each {
      if(!it.isRelationField()) {
        out.println "    ${it.name}"
      }
    }
    out.println "  }"
    out.println "  total: ${schemaName}_aggregate_${modelName}(where: \$where) @transform(get: \"_count\") {"
    out.println "    _count"
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(String projectId, GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setProjectId(projectId)
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Fetch a paginated ${context.getModelClass().getName()} records list")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("GET")
    apiDefinition.setPath("/${context.getModelClass().getName()}/page")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyPaginationQuery",
        "query"        : generate(context).getFirst(),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
