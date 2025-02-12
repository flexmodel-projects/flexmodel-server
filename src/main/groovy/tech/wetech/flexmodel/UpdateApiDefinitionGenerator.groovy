package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiInfo
import tech.wetech.flexmodel.codegen.enumeration.ApiType

/**
 * @author cjbi
 */
class UpdateApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  def generate(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getModelName()
    out.println "mutation MyUpdateMutation("
    context.getModelClass().getBasicFields().each {
      out.print "\$${it.fieldName}: ${typeMapping[((TypedField) it.originalField).type]}, "
    }
    out.println "\$id: ID!) {"
    out.println "  ${schemaName}_update_${modelName}_by_id("
    out.println "    id: \$id"
    out.print "    _set: {"
    context.getModelClass().getBasicFields().each {
      out.print "${it.fieldName}: \$${it.fieldName}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println "}"
    out.println "  ) {"
    context.getModelClass().getAllFields().each {
      if (!it.isRelationField()) {
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
    apiInfo.setName("Update ${context.getModelClass().getModelName()} record")
    apiInfo.setType("API" as ApiType)
    apiInfo.setMethod("PUT")
    apiInfo.setPath("/${context.getModelClass().getModelName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyUpdateMutation",
        "query"        : generate(context),
      ]
    ]
    apiInfo.setMeta(meta)
    apiInfo.setEnabled(true)
    return apiInfo
  }
}
