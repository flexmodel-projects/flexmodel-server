package dev.flexmodel

import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition
import dev.flexmodel.codegen.enumeration.ApiType
import dev.flexmodel.model.field.TypedField

/**
 * @author cjbi
 */
class CreateApiDefinitionGenerator extends ApiDefinitionGenerator {

  @Override
  void write(PrintWriter out, GenerationContext context) {
    def schemaName = context.getModelClass().getSchemaName()
    def modelName = context.getModelClass().getName()
    out.println "mutation MyCreateMutation("
    context.getModelClass().getBasicFields().each {
      if(it.isIdentity()){
        out.print "\$${it.name}: ID"
      } else {
        out.print "\$${it.name}: ${typeMapping[((TypedField) it.original).type]}"
      }
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.println ") {"
    out.println "  ${schemaName}_create_${modelName}("
    out.print "    data: {"
    context.getModelClass().getBasicFields().each {
      out.print "${it.name}: \$${it.name}"
      if (context.modelClass.basicFields.last() != it) {
        out.print(", ")
      }
    }
    out.print " }"
    out.println ") {"
    context.getModelClass().getBasicFields().each {
      out.println "    ${it.name}"
    }
    out.println "  }"
    out.println "}"
  }

  @Override
  ApiDefinition createApiDefinition(String projectId, GenerationContext context) {
    ApiDefinition apiDefinition = new ApiDefinition()
    apiDefinition.setProjectId(projectId)
    apiDefinition.setParentId(context.getVariable("apiParentId"))
    apiDefinition.setName("Create ${context.getModelClass().getName()} record")
    apiDefinition.setType("API" as ApiType)
    apiDefinition.setMethod("POST")
    apiDefinition.setPath("/${context.getModelClass().getName()}")

    Map<String, Object> meta = [
      "auth"     : false,
      "execution": [
        "operationName": "MyCreateMutation",
        "query"        : generate(context).getFirst()
      ]
    ]
    apiDefinition.setMeta(meta)
    apiDefinition.setEnabled(true)
    return apiDefinition
  }

}
