package dev.flexmodel

import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition
import dev.flexmodel.codegen.enumeration.ApiType
import dev.flexmodel.model.field.TypedField

/**
 * @author cjbi
 */
class UpdateApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getName()
    def id = context.getVariable("idFieldOfPath")
    def idField = context.getModelClass().getField(id as String)
    out.println "mutation MyUpdateMutation("
    context.getModelClass().getBasicFields().each {
      out.print "\$${it.name}: ${typeMapping[((TypedField) it.original).type]}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println ") {"
    out.println "  ${schemaName}_update_${modelName}("
    out.println "  where: {${id}: {_eq: \$${id}} }, "
    out.print "    _set: {"
    context.getModelClass().getBasicFields().each {
      if (id != it.name) {
        out.print "${it.name}: \$${it.name}"
        if (context.modelClass.basicFields.last() != it) {
          out.print(", ")
        }
      }
    }
    out.println "}"
    out.println "  ) {"
    out.println "    affected_rows"
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(String projectId, GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setProjectId(projectId)
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Update ${context.getModelClass().getName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("PUT")
    apiDefinition.setPath("/${context.getModelClass().getName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyUpdateMutation",
        "query"        : generate(context).getFirst(),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
