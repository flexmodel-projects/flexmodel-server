package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.ModelingApplicationService;
import dev.flexmodel.model.*;
import dev.flexmodel.model.field.TypedField;

import java.util.List;

/**
 * @author cjbi
 */
@Tag(name = "模型", description = "模型管理")
@Path("/v1/projects/{projectId}/datasources/{datasourceName}/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelingResource {

  @Inject
  ModelingApplicationService modelingApplicationService;

  @Operation(summary = "获取模型列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          anyOf = {
            EnumSchema.class,
            NativeQuerySchema.class,
            EntitySchema.class
          }
        ),
        examples = {
          @ExampleObject(
            name = "实体",
            value = """
              [{ "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }]
              """
          ),
          @ExampleObject(
            name = "枚举",
            value = """
              [{ "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }]
              """
          ),
          @ExampleObject(
            name = "本地查询",
            value = """
              [{ "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }]
              """
          ),
        }
      )
    })
  @GET
  public List<SchemaObject> findModels(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName) {
    return modelingApplicationService.findModels(projectId, datasourceName);
  }

  @Operation(summary = "获取单个模型")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          anyOf = {
            EnumSchema.class,
            NativeQuerySchema.class,
            EntitySchema.class
          }
        ),
        examples = {
          @ExampleObject(
            name = "实体",
            value = """
              { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
              """
          ),
          @ExampleObject(
            name = "枚举",
            value = """
              { "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }
              """
          ),
          @ExampleObject(
            name = "本地查询",
            value = """
              { "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }
              """
          ),
        }
      )
    })
  @GET
  @Path("/{modelName}")
  public SchemaObject findModel(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName) {
    return modelingApplicationService.findModel(projectId, datasourceName, modelName);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        anyOf = {
          EnumSchema.class,
          NativeQuerySchema.class,
          EntitySchema.class
        }
      ),
      examples = {
        @ExampleObject(
          name = "实体",
          value = """
            { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
            """
        ),
        @ExampleObject(
          name = "枚举",
          value = """
            { "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }
            """
        ),
        @ExampleObject(
          name = "本地查询",
          value = """
            { "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }
            """
        ),
      }
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          anyOf = {
            EnumSchema.class,
            NativeQuerySchema.class,
            EntitySchema.class
          }
        ),
        examples = {
          @ExampleObject(
            name = "实体",
            value = """
              { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
              """
          ),
          @ExampleObject(
            name = "枚举",
            value = """
              { "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }
              """
          ),
          @ExampleObject(
            name = "本地查询",
            value = """
              { "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }
              """
          ),
        }
      )
    })
  @Operation(summary = "创建模型")
  @POST
  public SchemaObject createModel(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, SchemaObject model) {
    return modelingApplicationService.createModel(projectId, datasourceName, model);
  }

  @POST
  @Path("/idl/execute")
  public List<SchemaObject> executeIdl(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, IdlRequest request) {
    try {
      return modelingApplicationService.executeIdl(projectId, datasourceName, request.idl());
    } catch (Exception e) {
      throw new RuntimeException("IDL格式有误: " + e.getMessage());
    }
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        anyOf = {
          EnumSchema.class,
          NativeQuerySchema.class,
          EntitySchema.class
        }
      ),
      examples = {
        @ExampleObject(
          name = "实体",
          value = """
            { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
            """
        ),
        @ExampleObject(
          name = "枚举",
          value = """
            { "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }
            """
        ),
        @ExampleObject(
          name = "本地查询",
          value = """
            { "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }
            """
        ),
      }
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          anyOf = {
            EnumSchema.class,
            NativeQuerySchema.class,
            EntitySchema.class
          }
        ),
        examples = {
          @ExampleObject(
            name = "实体",
            value = """
              { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "Long", "identity": true,  "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentName", "type": "String", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "EnumRef", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "EnumRef", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "Int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "Long", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "Relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
              """
          ),
          @ExampleObject(
            name = "枚举",
            value = """
              { "name": "UserGender", "type": "enum", "elements": [ "UNKNOWN", "MALE", "FEMALE" ], "comment": "性别" }
              """
          ),
          @ExampleObject(
            name = "本地查询",
            value = """
              { "name": "分组查询", "type": "native_query", "statement": "select count(id) as total, gender, max(age) as ageSum from Student group by gender" }
              """
          ),
        }
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "更新模型")
  @PUT
  @Path("/{modelName}")
  public SchemaObject modifyModel(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, SchemaObject model) {
    return modelingApplicationService.modifyModel(projectId, datasourceName, modelName, model);
  }


  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "删除模型")
  @DELETE
  @Path("/{modelName}")
  public void dropModel(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName) {
    modelingApplicationService.dropModel(projectId, datasourceName, modelName);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = TypedFieldSchema.class)
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = TypedFieldSchema.class)
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "创建字段")
  @POST
  @Path("/{modelName}/fields")
  public TypedField<?, ?> createField(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    return modelingApplicationService.createField(projectId, datasourceName, field);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = TypedFieldSchema.class)
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = TypedFieldSchema.class)
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "fieldName", description = "字段名称", in = ParameterIn.PATH)
  @Operation(summary = "更新字段")
  @PUT
  @Path("/{modelName}/fields/{fieldName}")
  public TypedField<?, ?> modifyField(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    field.setName(fieldName);
    return modelingApplicationService.modifyField(projectId, datasourceName, field);
  }

  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "fieldName", description = "字段名称", in = ParameterIn.PATH)
  @Operation(summary = "删除字段")
  @DELETE
  @Path("/{modelName}/fields/{fieldName}")
  public void dropField(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName) {
    modelingApplicationService.dropField(projectId, datasourceName, modelName, fieldName);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = IndexSchema.class),
      examples = {
        @ExampleObject(value = """
        { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
          """)
      }
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = IndexSchema.class),
        examples = {
          @ExampleObject(value = """
          { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
            """)
        }
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "创建索引")
  @POST
  @Path("/{modelName}/indexes")
  public IndexDefinition createIndex(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, IndexDefinition index) {
    index.setModelName(modelName);
    return modelingApplicationService.createIndex(projectId, datasourceName, index);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = IndexSchema.class),
      examples = {
        @ExampleObject(value = """
        { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
          """)
      }
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = IndexSchema.class),
        examples = {
          @ExampleObject(value = """
          { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
            """)
        }
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "indexName", description = "索引名称", in = ParameterIn.PATH)
  @Operation(summary = "更新索引")
  @PUT
  @Path("/{modelName}/indexes/{indexName}")
  public IndexDefinition modifyIndex(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, @PathParam("indexName") String indexName, IndexDefinition index) {
    index.setModelName(modelName);
    index.setName(indexName);
    return modelingApplicationService.modifyIndex(projectId, datasourceName, index);
  }

  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "indexName", description = "索引名称", in = ParameterIn.PATH)
  @Operation(summary = "删除索引")
  @DELETE
  @Path("/{modelName}/indexes/{indexName}")
  public void dropIndex(@PathParam("projectId") String projectId, @PathParam("datasourceName") String datasourceName, @PathParam("modelName") String modelName, @PathParam("indexName") String indexName) {
    modelingApplicationService.dropIndex(projectId, datasourceName, modelName, indexName);
  }


  public record IdlRequest(String idl) {

  }

  @Schema(
    description = "索引，更多信息见Schema定义文档",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "fields", description = "字段列表"),
      @SchemaProperty(name = "unique", description = "是否唯一"),
    }
  )
  public static class IndexSchema extends IndexDefinition {

    public IndexSchema(String name) {
      super(name);
    }

  }

  @Schema(
    description = "类型字段，更多信息见Schema定义文档",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "type", description = "类型"),
      @SchemaProperty(name = "comment", description = "注释"),
      @SchemaProperty(name = "unique", description = "是否唯一"),
      @SchemaProperty(name = "nullable", description = "可为空"),
      @SchemaProperty(name = "defaultValue", description = "默认值"),
      @SchemaProperty(name = "additionalProperties", description = "用户自定义扩展属性"),
    }
  )
  public static class TypedFieldSchema extends TypedField {

    public TypedFieldSchema(String name, String type) {
      super(name, type);
    }
  }

  @Schema(
    description = "枚举",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "comment", description = "注释"),
      @SchemaProperty(name = "elements", description = "元素列表"),
      @SchemaProperty(name = "additionalProperties", description = "用户自定义扩展属性"),
    }
  )
  public static class EnumSchema extends EnumDefinition {

    public EnumSchema(String name) {
      super(name);
    }
  }

  @Schema(
    description = "本地查询",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "type", description = "类型, entity: 实体；native_query: 本地查询；enum: 枚举"),
      @SchemaProperty(name = "statement", description = "语句"),
      @SchemaProperty(name = "comment", description = "注释"),
      @SchemaProperty(name = "additionalProperties", description = "用户自定义扩展属性"),
    }
  )
  public static class NativeQuerySchema extends NativeQueryDefinition {

    public NativeQuerySchema(String name) {
      super(name);
    }
  }

  @Schema(
    description = "实体",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "type", description = "类型, entity: 实体；native_query: 本地查询；enum: 枚举"),
      @SchemaProperty(name = "fields", description = "字段列表"),
      @SchemaProperty(name = "indexes", description = "索引列表"),
      @SchemaProperty(name = "comment", description = "注释"),
      @SchemaProperty(name = "additionalProperties", description = "用户自定义扩展属性"),
    }
  )
  public static class EntitySchema extends EntityDefinition {

    public EntitySchema(String name) {
      super(name);
    }
  }

}
