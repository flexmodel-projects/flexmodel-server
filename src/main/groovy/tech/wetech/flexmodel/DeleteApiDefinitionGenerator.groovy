package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition
import tech.wetech.flexmodel.codegen.enumeration.ApiType
import tech.wetech.flexmodel.model.field.TypedField

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
  ApiDefinition createApiDefinition(GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
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
