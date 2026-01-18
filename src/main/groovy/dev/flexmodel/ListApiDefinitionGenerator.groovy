package dev.flexmodel

import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition
import dev.flexmodel.codegen.enumeration.ApiType

/**
 * @author cjbi
 */
class ListApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getName()
    out.println "query MyListQuery( \$where: ${schemaName}_${modelName}_bool_exp) {"
    out.println "  list: ${schemaName}_list_${modelName}(where: \$where) {"
    context.getModelClass().getAllFields().each {
      if (!it.isRelationField()) {
        out.println "    ${it.name}"
      }
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(String projectId, GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setProjectId(projectId)
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Fetch ${context.getModelClass().getName()} records list")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("GET")
    apiDefinition.setPath("/${context.getModelClass().getName()}/list")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyListQuery",
        "query"        : generate(context).getFirst(),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
