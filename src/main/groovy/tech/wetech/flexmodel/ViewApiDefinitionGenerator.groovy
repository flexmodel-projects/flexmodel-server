package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition
import tech.wetech.flexmodel.codegen.enumeration.ApiType

/**
 * @author cjbi
 */
class ViewApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  def generate(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getModelName()
    out.println "query MyViewQuery( \$where: ${schemaName}_${modelName}_bool_exp) {"
    out.println "  ${schemaName}_find_one_${modelName}(where: \$where) {"
    context.getModelClass().getAllFields().each {
      if(!it.isRelationField()) {
        out.println "    ${it.variableName}"
      }
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Fetch a single ${context.getModelClass().getModelName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("GET")
    apiDefinition.setPath("/${context.getModelClass().getModelName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyViewQuery",
        "query"        : generate(context),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
