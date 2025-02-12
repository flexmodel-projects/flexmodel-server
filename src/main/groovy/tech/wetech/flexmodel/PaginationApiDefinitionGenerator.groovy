package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiInfo
import tech.wetech.flexmodel.codegen.enumeration.ApiType

/**
 * @author cjbi
 */
class PaginationApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  def generate(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getModelName()
    out.println "query MyPaginationQuery( \$where: ${schemaName}_${modelName}_bool_exp, \$page: Int = 1, \$size: Int = 10) {"
    out.println "  list: ${schemaName}_list_${modelName}(where: \$where, page: \$page, size: \$size) {"
    context.getModelClass().getAllFields().each {
      if(!it.isRelationField()) {
        out.println "    ${it.fieldName}"
      }
    }
    out.println "  }"
    out.println "  total: ${schemaName}_aggregate_${modelName}(where: \$where) @transform(get: \"_count\") {"
    out.println "    _count"
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiInfo createApiInfo(GenerationContext context) {
    ApiInfo apiInfo = new ApiInfo()
    apiInfo.setParentId(context.getVariable("apiParentId"))
    apiInfo.setName("Fetch a paginated ${context.getModelClass().getModelName()} records list")
    apiInfo.setType("API" as ApiType)
    apiInfo.setMethod("GET")
    apiInfo.setPath("/${context.getModelClass().getModelName()}/page")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyPaginationQuery",
        "query"        : generate(context),
      ]
    ]
    apiInfo.setMeta(meta)
    apiInfo.setEnabled(true)
    return apiInfo
  }
}
