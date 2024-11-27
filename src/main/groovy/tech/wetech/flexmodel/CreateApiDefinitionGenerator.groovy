package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiInfo

/**
 * @author cjbi
 */
class CreateApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  def generate(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getModelName()
    out.println "mutation MyCreateMutation("
    context.getModelClass().getBasicFields().each {
      out.print "\$${it.fieldName}: ${typeMapping[((TypedField) it.originalField).type]}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println ") {"
    out.println "  ${schemaName}_create_${modelName}("
    out.print "    data: {"
    context.getModelClass().getBasicFields().each {
      out.print "${it.fieldName}: \$${it.fieldName}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.print " }"
    out.println ") {"
    context.getModelClass().getBasicFields().each {
      out.println "    ${it.fieldName}"
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiInfo createApiInfo(GenerationContext context) {
    ApiInfo apiInfo = new ApiInfo()
    apiInfo.setParentId(context.getVariable("apiParentId"))
    apiInfo.setName("Create ${context.getModelClass().getModelName()} record")
    apiInfo.setType("API")
    apiInfo.setMethod("POST")
    apiInfo.setPath("/${context.getModelClass().getModelName()}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyCreateMutation",
        "query"        : generate(context)
      ]
    ]
    apiInfo.setMeta(meta)
    apiInfo.setEnabled(true)
    return apiInfo
  }

}
