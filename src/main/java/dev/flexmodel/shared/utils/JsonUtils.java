package dev.flexmodel.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.flexmodel.supports.jackson.FlexmodelCoreModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

/**
 * @author cjbi
 */
public class JsonUtils {

  private final JsonMapper jsonMapper;

  private static JsonUtils jsonUtils;

  public JsonUtils() {
    JsonMapper.Builder builder = new JsonMapper().rebuild();
    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
//        JSON.configure(SerializationFeature.INDENT_OUTPUT, false);
    //不显示为null的字段
    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    builder.disable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    builder.disable(FAIL_ON_EMPTY_BEANS);
    builder.addModule(new JavaTimeModule());
    builder.addModule(new FlexmodelCoreModule());
    ServiceLoader.load(Module.class).forEach(builder::addModule);
    this.jsonMapper = builder.build();
  }

  public static JsonUtils getInstance() {
    if (jsonUtils == null) {
      jsonUtils = new JsonUtils();
    }
    return jsonUtils;
  }

  public String stringify(Object obj) {
    try {
      return jsonMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public <T> T parseToObject(String json, Class<T> clz) {
    try {
      if (json == null) {
        return null;
      }
      return jsonMapper.readValue(json, clz);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public <T> List<T> parseToList(String json, Class<T> clz) {
    return convertValueList(parseToObject(json, List.class), clz);
  }

  public <T> T convertValue(Object fromValue, Class<T> clz) {
    try {
      return jsonMapper.convertValue(fromValue, clz);
    } catch (Exception e) {
      System.err.println("Failed to convert value: " + fromValue + " to " + clz.getName());
      System.err.println("Error: " + e.getMessage());
      throw e;
    }
  }

  public <T> List<T> convertValueList(List<?> fromValues, Class<T> cls) {
    List<T> list = new ArrayList<>();
    for (Object fromValue : fromValues) {
      list.add(convertValue(fromValue, cls));
    }
    return list;
  }

}
