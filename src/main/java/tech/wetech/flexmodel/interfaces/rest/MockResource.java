package tech.wetech.flexmodel.interfaces.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.Map;

/**
 * @author cjbi
 */
@Path("/fm-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MockResource {

  @GET
  @Path("/user/refresh-permissions")
  public Object permissions() {
    return JsonUtils.getInstance().parseToObject("""
      {
          "code": 0,
          "data": {
              "token": "mock_token_123456",
              "user": {
                  "id": 1,
                  "username": "admin",
                  "email": "1275093225@qq.com",
                  "phone": "123456789"
              },
              "permissions": [
                  "/dashboard",
                  "/demo",
                  "/demo/copy",
                  "/demo/editor",
                  "/demo/wangEditor",
                  "/demo/virtualScroll",
                  "/demo/watermark",
                  "/demo/dynamic",
                  "/demo/level",
                  "/authority/user",
                  "/authority/user/index",
                  "/authority/user/create",
                  "/authority/user/update",
                  "/authority/user/view",
                  "/authority/user/delete",
                  "/authority/user/authority",
                  "/authority/role",
                  "/authority/role/index",
                  "/authority/role/create",
                  "/authority/role/update",
                  "/authority/role/view",
                  "/authority/role/delete",
                  "/authority/menu",
                  "/authority/menu/index",
                  "/authority/menu/create",
                  "/authority/menu/update",
                  "/authority/menu/view",
                  "/authority/menu/delete",
                  "/content/article",
                  "/content/article/index",
                  "/content/article/create",
                  "/content/article/update",
                  "/content/article/view",
                  "/content/article/delete"
              ]
          }
      }
      """, Map.class);
  }

  @POST
  @Path("/user/login")
  public Object userLogin(){
    return JsonUtils.getInstance().parseToObject("""
     {
          "code": 0,
          "data": {
              "token": "mock_token_123456",
              "user": {
                  "id": 1,
                  "username": "admin",
                  "email": "1275093225@qq.com",
                  "phone": "123456789"
              },
              "permissions": [
                  "/dashboard",
                  "/demo",
                  "/demo/copy",
                  "/demo/editor",
                  "/demo/wangEditor",
                  "/demo/virtualScroll",
                  "/demo/watermark",
                  "/authority/user",
                  "/authority/user/index",
                  "/authority/user/create",
                  "/authority/user/update",
                  "/authority/user/view",
                  "/authority/user/delete",
                  "/authority/user/authority",
                  "/authority/role",
                  "/authority/role/index",
                  "/authority/role/create",
                  "/authority/role/update",
                  "/authority/role/view",
                  "/authority/role/delete",
                  "/authority/menu",
                  "/authority/menu/index",
                  "/authority/menu/create",
                  "/authority/menu/update",
                  "/authority/menu/view",
                  "/authority/menu/delete",
                  "/content/article",
                  "/content/article/index",
                  "/content/article/create",
                  "/content/article/update",
                  "/content/article/view",
                  "/content/article/delete"
              ]
          }
          }
      """, Map.class);
  }

}
