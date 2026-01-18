package dev.flexmodel

import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition
import dev.flexmodel.codegen.enumeration.ApiType
import dev.flexmodel.model.field.TypedField

/**
 * @author cjbi
 */
class DeleteApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getName()
    def id = context.getVariable("idFieldOfPath")
    def idField = context.getModelClass().getField(id as String)
    out.println "mutation MyDeleteMutation("
    out.print "\$${idField.name}: ${typeMapping[((TypedField) idField.original).type]}"
    out.println ") {"
    out.println "  ${schemaName}_delete_${modelName}(where: {${id}: {_eq: \$${id}} } ) {"
    out.println "    affected_rows"
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(String projectId, GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setProjectId(projectId)
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Delete ${context.getModelClass().getName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("DELETE")
    apiDefinition.setPath("/${context.getModelClass().getName()}/{id}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyDeleteMutation",
        "query"        : generate(context).getFirst(),
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }
}
