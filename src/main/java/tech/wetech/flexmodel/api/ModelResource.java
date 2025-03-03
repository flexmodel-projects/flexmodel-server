package tech.wetech.flexmodel.api;

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
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.application.ModelingApplicationService;

import java.util.List;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Tag(name = "模型", description = "模型管理")
@Path(BASE_PATH + "/datasources/{datasourceName}/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

  @Parameter(name = "datasourceName", description = "数据源名称", in = ParameterIn.PATH)
  @PathParam("datasourceName")
  String datasourceName;

  @Inject
  ModelingApplicationService modelingApplicationService;

  @Operation(summary = "获取模型列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
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
              [{ "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "id", "modelName": "Student", "unique": false, "nullable": true, "generatedValue": "BIGINT_NOT_GENERATED" }, { "name": "studentName", "type": "string", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "enum", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "enum", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "bigint", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }]
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
  public List<TypeWrapper> findModels() {
    return modelingApplicationService.findModels(datasourceName);
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
            { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "id", "modelName": "Student", "unique": false, "nullable": true, "generatedValue": "BIGINT_NOT_GENERATED" }, { "name": "studentName", "type": "string", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "enum", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "enum", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "bigint", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
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
    description = "成功",
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
              { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "id", "modelName": "Student", "unique": false, "nullable": true, "generatedValue": "BIGINT_NOT_GENERATED" }, { "name": "studentName", "type": "string", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "enum", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "enum", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "bigint", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
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
  public TypeWrapper createModel(
    TypeWrapper model) {
    return modelingApplicationService.createModel(datasourceName, model);
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
            { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "id", "modelName": "Student", "unique": false, "nullable": true, "generatedValue": "BIGINT_NOT_GENERATED" }, { "name": "studentName", "type": "string", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "enum", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "enum", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "bigint", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
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
    description = "成功",
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
              { "type": "entity", "name": "Student", "fields": [ { "name": "id", "type": "id", "modelName": "Student", "unique": false, "nullable": true, "generatedValue": "BIGINT_NOT_GENERATED" }, { "name": "studentName", "type": "string", "modelName": "Student", "unique": false, "nullable": true, "length": 255 }, { "name": "gender", "type": "enum", "from": "UserGender", "multiple": false, "modelName": "Student", "unique": false, "nullable": true }, { "name": "interest", "type": "enum", "from": "user_interest", "multiple": true, "modelName": "Student", "unique": false, "nullable": true }, { "name": "age", "type": "int", "modelName": "Student", "unique": false, "nullable": true }, { "name": "classId", "type": "bigint", "modelName": "Student", "unique": false, "nullable": true }, { "name": "studentDetail", "type": "relation", "modelName": "Student", "unique": false, "nullable": true, "multiple": false, "from": "StudentDetail", "localField": "id", "foreignField": "studentId", "cascadeDelete": true } ], "indexes": [ { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false } ] }
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
  public TypeWrapper modifyModel(@PathParam("modelName") String modelName, TypeWrapper model) {
    return modelingApplicationService.modifyModel(datasourceName, modelName, model);
  }


  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "删除模型")
  @DELETE
  @Path("/{modelName}")
  public void dropModel(@PathParam("modelName") String modelName) {
    modelingApplicationService.dropModel(datasourceName, modelName);
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
    description = "成功",
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
  public TypedField<?, ?> createField(@PathParam("modelName") String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    return modelingApplicationService.createField(datasourceName, field);
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
    description = "成功",
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
  public TypedField<?, ?> modifyField(@PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    field.setName(fieldName);
    return modelingApplicationService.modifyField(datasourceName, field);
  }

  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "fieldName", description = "字段名称", in = ParameterIn.PATH)
  @Operation(summary = "删除字段")
  @DELETE
  @Path("/{modelName}/fields/{fieldName}")
  public void dropField(@PathParam("modelName") String modelName, @PathParam("fieldName") String fieldName) {
    modelingApplicationService.dropField(datasourceName, modelName, fieldName);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = IndexSchema.class),
      example = """
        { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
        """
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = IndexSchema.class),
        example = """
          { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
          """
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Operation(summary = "创建索引")
  @POST
  @Path("/{modelName}/indexes")
  public Index createIndex(@PathParam("modelName") String modelName, Index index) {
    index.setModelName(modelName);
    return modelingApplicationService.createIndex(datasourceName, index);
  }

  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(implementation = IndexSchema.class),
      example = """
        { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
        """
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = IndexSchema.class),
        example = """
          { "modelName": "Student", "name": "IDX_studentName", "fields": [ { "fieldName": "studentName", "direction": "ASC" } ], "unique": false }
          """
      )
    })
  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "indexName", description = "索引名称", in = ParameterIn.PATH)
  @Operation(summary = "更新索引")
  @PUT
  @Path("/{modelName}/indexes/{indexName}")
  public Index modifyIndex(@PathParam("modelName") String modelName, @PathParam("indexName") String indexName, Index index) {
    index.setModelName(modelName);
    index.setName(indexName);
    return modelingApplicationService.modifyIndex(datasourceName, index);
  }

  @Parameter(name = "modelName", description = "模型名称", in = ParameterIn.PATH)
  @Parameter(name = "indexName", description = "索引名称", in = ParameterIn.PATH)
  @Operation(summary = "删除索引")
  @DELETE
  @Path("/{modelName}/indexes/{indexName}")
  public void dropIndex(@PathParam("modelName") String modelName, @PathParam("indexName") String indexName) {
    modelingApplicationService.dropIndex(datasourceName, modelName, indexName);
  }


  @Schema(
    description = "索引，更多信息见Schema定义文档",
    properties = {
      @SchemaProperty(name = "name", description = "名称"),
      @SchemaProperty(name = "fields", description = "字段列表"),
      @SchemaProperty(name = "unique", description = "是否唯一"),
    }
  )
  public static class IndexSchema extends Index {

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
  public static class EnumSchema extends Enum {

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
  public static class NativeQuerySchema extends NativeQueryModel {

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
  public static class EntitySchema extends Entity {

    public EntitySchema(String name) {
      super(name);
    }
  }

}
