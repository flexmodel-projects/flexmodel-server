package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.AbstractGenerator
import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition

/**
 * @author cjbi
 */
abstract class ApiDefinitionGenerator extends AbstractGenerator {

  protected def typeMapping = [
    "id"      : "ID",
    "string"  : "String",
    "text"    : "String",
    "decimal" : "Float",
    "int"     : "Int",
    "bigint"  : "Int",
    "boolean" : "Boolean",
    "datetime": "String",
    "date"    : "String",
    "json"    : "JSON",
  ]

  abstract ApiDefinition createApiDefinition(GenerationContext context)

}
