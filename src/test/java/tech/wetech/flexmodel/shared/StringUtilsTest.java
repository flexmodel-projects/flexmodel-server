package dev.flexmodel.shared;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.shared.utils.StringUtils;

import java.util.Map;

/**
 * @author cjbi
 */
class StringUtilsTest {

  @Test
  void testSimpleRenderTemplate() {
    Map<String, String> params = Map.of(
      "foo", "hello",
      "bar", "world"
    );
    String template = "${foo}";
    Assertions.assertEquals("hello", StringUtils.simpleRenderTemplate(template, params));
    String template2 = " ${bar} ";
    Assertions.assertEquals(" world ", StringUtils.simpleRenderTemplate(template2, params));
    String template3 = "${foo} ${bar}";
    Assertions.assertEquals("hello world", StringUtils.simpleRenderTemplate(template3, params));
  }
}
