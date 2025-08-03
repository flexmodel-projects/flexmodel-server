package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition
import tech.wetech.flexmodel.codegen.enumeration.ApiType
import tech.wetech.flexmodel.model.field.TypedField

/**
 * @author cjbi
 */
class UpdateApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getModelName()
    out.println "mutation MyUpdateMutation("
    context.getModelClass().getBasicFields().each {
      out.print "\$${it.variableName}: ${typeMapping[((TypedField) it.originalField).type]}, "
    }
    out.println "\$id: ID!) {"
    out.println "  ${schemaName}_update_${modelName}_by_id("
    out.println "    id: \$id"
    out.print "    _set: {"
    context.getModelClass().getBasicFields().each {
      out.print "${it.variableName}: \$${it.variableName}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println "}"
    out.println "  ) {"
    context.getModelClass().getAllFields().each {
      if (!it.isRelationField()) {
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
    apiDefinition.setName("Update ${context.getModelClass().getModelName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("PUT")
    apiDefinition.setPath("/${context.getModelClass().getModelName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyUpdateMutation",
        "query"        : generate(context),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
