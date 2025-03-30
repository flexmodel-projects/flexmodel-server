package tech.wetech.flexmodel

import tech.wetech.flexmodel.codegen.AbstractGenerator
import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.entity.ApiDefinition

import static tech.wetech.flexmodel.ScalarType.*

/**
 * @author cjbi
 */
abstract class ApiDefinitionGenerator extends AbstractGenerator {

  protected def typeMapping = [
    (ID.type)      : "ID",
    (STRING.type)  : "String",
    (FLOAT.type)   : "Float",
    (INT.type)     : "Int",
    (LONG.type)    : "Int",
    (BOOLEAN.type) : "Boolean",
    (DATETIME.type): "String",
    (DATE.type)    : "String",
    (TIME.type)    : "String",
    (JSON.type)    : "JSON",
  ]

  abstract ApiDefinition createApiDefinition(GenerationContext context)

}
