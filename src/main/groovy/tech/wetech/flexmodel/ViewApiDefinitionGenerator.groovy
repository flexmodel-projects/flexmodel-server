package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiInfo

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
        out.println "    ${it.fieldName}"
      }
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiInfo createApiInfo(GenerationContext context) {
    ApiInfo apiInfo = new ApiInfo()
    apiInfo.setParentId(context.getVariable("apiParentId"))
    apiInfo.setName("Fetch a single ${context.getModelClass().getModelName()} record")
    apiInfo.setType("API")
    apiInfo.setMethod("GET")
    apiInfo.setPath("/${context.getModelClass().getModelName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyViewQuery",
        "query"        : generate(context),
      ]
    ]
    apiInfo.setMeta(meta)
    apiInfo.setEnabled(true)
    return apiInfo
  }
}
