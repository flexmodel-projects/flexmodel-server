package tech.wetech.flexmodel.domain.model.flow.util;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.domain.model.flow.shared.util.GroovyUtil;

import java.util.Map;

@Slf4j
public class GroovyUtilTest {

  /**
   * Exception:
   * int i = i;
   * String String == '111';
   * String.equals111('123')
   * int a = '123';
   * curl http://www.a.com
   * abc
   * 1/0
   *
   */
  @Test
  public void fun1() {
    try {
      String expression = "";
      Map<String, Object> dataMap = Maps.newHashMap();
      Object result = GroovyUtil.execute(expression, dataMap);
      Assertions.assertNull(result);
    } catch (Exception e) {
      log.warn("catch exception", e);
    }
  }
}
