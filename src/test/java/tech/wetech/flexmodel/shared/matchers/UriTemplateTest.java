package tech.wetech.flexmodel.shared.matchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author chengjinbao
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
