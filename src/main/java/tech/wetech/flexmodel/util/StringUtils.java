package tech.wetech.flexmodel.util;

import java.util.Map;

/**
 * @author cjbi
 */
public class StringUtils {

  public static String simpleRenderTemplate(String template, Map<?, ?> attributes) {
    if (template == null) {
      return null;
    }
    int length = template.length();
    for (int i = 0; i < length; i++) {
      if (template.charAt(i) == '$') {
        if (length > i + 1) {
          int j = i;
          char c = template.charAt(++j);
          if (c == '{') {
            template = simpleRenderTemplate(template, length, ++j, attributes);
            length = template.length();
          }
        }
      }
    }
    return template;
  }

  private static String simpleRenderTemplate(String template, int length, int i, Map<?, ?> attributes) {
    StringBuilder valueBuilder = new StringBuilder();
    int endIndex = i - 2;
    label:
    for (; i < length; i++) {
      char c1 = template.charAt(i);
      switch (c1) {
        case ' ':
          continue;
        case '}':
          break label;
        default:
          valueBuilder.append(c1);
      }
    }
    String keyString = valueBuilder.toString();
    Object value = attributes;
    if (attributes.get(keyString) instanceof String) {
      value = attributes.get(keyString);
    } else {
      String[] keys = keyString.split("\\.");
      for (String key : keys) {
        if (value instanceof Map) {
          value = ((Map<?, ?>) value).get(key);
        } else {
          value = null;
        }
      }
    }
    return template.substring(0, endIndex) + value + template.substring(++i);
  }

}
