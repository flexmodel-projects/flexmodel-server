# Flexmodel Server 代码模板

## REST Resource 模板
```java
package dev.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cjbi
 */
@Slf4j
@Path("/{resource-name}")
@ApplicationScoped
public class {ResourceName}Resource {

  @Inject
  {ServiceName}Service service;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get{ResourceName}() {
    log.info("获取{resource-description}");
    return Response.ok(service.get{ResourceName}()).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create{ResourceName}({ResourceName}Request request) {
    log.info("创建{resource-description}: {}", request);
    return Response.ok(service.create{ResourceName}(request)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response update{ResourceName}(@PathParam("id") String id, {ResourceName}Request request) {
    log.info("更新{resource-description}: id={}, request={}", id, request);
    return Response.ok(service.update{ResourceName}(id, request)).build();
  }

  @DELETE
  @Path("/{id}")
  public Response delete{ResourceName}(@PathParam("id") String id) {
    log.info("删除{resource-description}: id={}", id);
    service.delete{ResourceName}(id);
    return Response.noContent().build();
  }
}
```

## Application Service 模板
```java
package dev.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class {ServiceName}ApplicationService {

  @Inject
  {RepositoryName}Repository repository;

  public {ResponseType} get{ResourceName}() {
    log.debug("获取{resource-description}");
    return repository.findAll();
  }

  public {ResponseType} create{ResourceName}({RequestType} request) {
    log.debug("创建{resource-description}: {}", request);
    // 业务逻辑处理
    return repository.save(request);
  }

  public {ResponseType} update{ResourceName}(String id, {RequestType} request) {
    log.debug("更新{resource-description}: id={}, request={}", id, request);
    // 业务逻辑处理
    return repository.update(id, request);
  }

  public void delete{ResourceName}(String id) {
    log.debug("删除{resource-description}: id={}", id);
    repository.deleteById(id);
  }
}
```

## Domain Model 模板
```java
package dev.flexmodel.domain.model.{domain};

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @author cjbi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class {ModelName} {
  
  private String id;
  private String name;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  // 业务方法
  public boolean isValid() {
    return name != null && !name.trim().isEmpty();
  }
  
  public void updateDescription(String description) {
    this.description = description;
    this.updatedAt = LocalDateTime.now();
  }
}
```

## Repository 接口模板
```java
package dev.flexmodel.domain.model.{domain};

import java.util.List;
import java.util.Optional;

/**
 * @author cjbi
 */
public interface {ModelName}Repository {
  
  List<{ModelName}> findAll();
  
  Optional<{ModelName}> findById(String id);
  
  {ModelName} save({ModelName} {modelName});
  
  {ModelName} update(String id, {ModelName} {modelName});
  
  void deleteById(String id);
  
  List<{ModelName}> findBy{FieldName}(String {fieldName});
}
```

## 测试类模板
```java
package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class {ResourceName}ResourceTest {

  @Test
  void testGet{ResourceName}() {
    given()
      .when()
      .get(Resources.ROOT_PATH + "/{resource-path}")
      .then()
      .statusCode(200)
      .body("size()", equalTo(0)); // 验证返回空列表
  }

  @Test
  void testCreate{ResourceName}() {
    {ResourceName}Request request = new {ResourceName}Request();
    request.setName("测试{resource-name}");
    request.setDescription("这是一个测试{resource-description}");

    given()
      .contentType("application/json")
      .body(request)
      .when()
      .post(Resources.ROOT_PATH + "/{resource-path}")
      .then()
      .statusCode(200)
      .body("name", equalTo("测试{resource-name}"))
      .body("description", equalTo("这是一个测试{resource-description}"));
  }

  @Test
  void testUpdate{ResourceName}() {
    // 先创建一个{resource-name}
    {ResourceName}Request createRequest = new {ResourceName}Request();
    createRequest.setName("原始{resource-name}");
    
    String id = given()
      .contentType("application/json")
      .body(createRequest)
      .when()
      .post(Resources.ROOT_PATH + "/{resource-path}")
      .then()
      .statusCode(200)
      .extract()
      .path("id");

    // 然后更新它
    {ResourceName}Request updateRequest = new {ResourceName}Request();
    updateRequest.setName("更新后的{resource-name}");
    updateRequest.setDescription("更新后的描述");

    given()
      .contentType("application/json")
      .body(updateRequest)
      .when()
      .put(Resources.ROOT_PATH + "/{resource-path}/" + id)
      .then()
      .statusCode(200)
      .body("name", equalTo("更新后的{resource-name}"))
      .body("description", equalTo("更新后的描述"));
  }

  @Test
  void testDelete{ResourceName}() {
    // 先创建一个{resource-name}
    {ResourceName}Request request = new {ResourceName}Request();
    request.setName("待删除的{resource-name}");
    
    String id = given()
      .contentType("application/json")
      .body(request)
      .when()
      .post(Resources.ROOT_PATH + "/{resource-path}")
      .then()
      .statusCode(200)
      .extract()
      .path("id");

    // 然后删除它
    given()
      .when()
      .delete(Resources.ROOT_PATH + "/{resource-path}/" + id)
      .then()
      .statusCode(204);

    // 验证已删除
    given()
      .when()
      .get(Resources.ROOT_PATH + "/{resource-path}/" + id)
      .then()
      .statusCode(404);
  }
}
```

## 配置类模板
```java
package dev.flexmodel.shared;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;

/**
 * @author cjbi
 */
@ConfigMapping(prefix = "flexmodel.{config-prefix}")
public interface {ConfigName}Config {

  @WithDefault("default-value")
  @WithName("property-name")
  String propertyName();

  @WithDefault("10")
  @WithName("max-count")
  int maxCount();

  @WithName("optional-property")
  Optional<String> optionalProperty();

  @WithDefault("true")
  @WithName("enabled")
  boolean enabled();
}
```

## 异常类模板

```java
package dev.flexmodel.domain.model.

{domain};

import dev.flexmodel.domain.model.BusinessException;

/**
 * @author cjbi
 */
public abstract class {DomainName}Exception extends

    BusinessException {

        public {
            DomainName
        } Exception(String message) {
            super(message);
        }

        public {
            DomainName
        } Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

/**
 * @author cjbi
 */
    public class {ModelName}NotFoundException extends{DomainName}

    Exception {

        public {
            ModelName
        } NotFoundException(String id) {
            super("未找到ID为 " + id + " 的{model-description}");
        }
    }

/**
 * @author cjbi
 */
    public class {ModelName}ValidationException extends{DomainName}

    Exception {

        public {
            ModelName
        } ValidationException(String message) {
            super("数据验证失败: " + message);
        }
    }
```

## DTO 模板
```java
package dev.flexmodel.interfaces.rest.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @author cjbi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class {ModelName}Request {
  
  private String name;
  private String description;
  // 其他字段...
}

package dev.flexmodel.interfaces.rest.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class {ModelName}Response {
  
  private String id;
  private String name;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  // 其他字段...
}
```

## 常用导入语句
```java
// REST Resource 常用导入
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.extern.slf4j.Slf4j;

// 测试常用导入
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

// 配置常用导入
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import io.smallrye.config.WithUnnamedKey;
import java.util.Map;
import java.util.Optional;

// 工具类常用导入
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
```
