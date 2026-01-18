package dev.flexmodel

import dev.flexmodel.codegen.AbstractGenerator
import dev.flexmodel.codegen.GenerationContext
import dev.flexmodel.codegen.entity.ApiDefinition

import static dev.flexmodel.model.field.ScalarType.*

/**
 * @author cjbi
 */
abstract class ApiDefinitionGenerator extends AbstractGenerator {

  protected def typeMapping = [
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

  abstract ApiDefinition createApiDefinition(String projectId, GenerationContext context)

}
