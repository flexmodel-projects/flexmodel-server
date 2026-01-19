package dev.flexmodel.shared.matchers;

import dev.flexmodel.shared.matchers.UriTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author cjbi
 */
class UriTemplateTest {

  @Test
  void testMain() {
    // .*是正则表达式
    UriTemplate uriTemplate = new UriTemplate("/api/{projectId}/.*");
    String path = "/api/dev_test/Classes/list";
    Map<String, String> match = uriTemplate.match(new UriTemplate(path));
    Assertions.assertNotNull(uriTemplate.match(new UriTemplate(path)));
  }
}
