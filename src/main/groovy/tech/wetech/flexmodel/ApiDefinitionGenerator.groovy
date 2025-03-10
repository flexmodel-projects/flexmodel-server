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
    (TEXT.type)    : "String",
    (DECIMAL.type) : "Float",
    (INT.type)     : "Int",
    (BIGINT.type)  : "Int",
    (BOOLEAN.type) : "Boolean",
    (DATETIME.type): "String",
    (DATE.type)    : "String",
    (JSON.type)    : "JSON",
  ]

  abstract ApiDefinition createApiDefinition(GenerationContext context)

}
