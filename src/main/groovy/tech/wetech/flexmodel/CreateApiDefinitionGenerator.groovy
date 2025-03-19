package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition
import tech.wetech.flexmodel.codegen.enumeration.ApiType

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
      out.print "\$${it.variableName}: ${typeMapping[((TypedField) it.originalField).type]}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println ") {"
    out.println "  ${schemaName}_create_${modelName}("
    out.print "    data: {"
    context.getModelClass().getBasicFields().each {
      out.print "${it.variableName}: \$${it.variableName}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.print " }"
    out.println ") {"
    context.getModelClass().getBasicFields().each {
      out.println "    ${it.variableName}"
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Create ${context.getModelClass().getModelName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("POST")
    apiDefinition.setPath("/${context.getModelClass().getModelName()}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyCreateMutation",
        "query"        : generate(context)
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }

}
