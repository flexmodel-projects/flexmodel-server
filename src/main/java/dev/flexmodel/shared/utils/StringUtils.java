package dev.flexmodel.shared.utils;

import java.util.*;

/**
 * @author cjbi
 */
public class StringUtils {

  private static final String[] EMPTY_STRING_ARRAY = {};

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

  /**
   * Tokenize the given {@code String} into a {@code String} array via a
   * {@link StringTokenizer}.
   * <p>The given {@code delimiters} string can consist of any number of
   * delimiter characters. Each of those characters can be used to separate
   * tokens. A delimiter is always a single character; for multi-character
   * delimiters.
   *
   * @param str               the {@code String} to tokenize (potentially {@code null} or empty)
   * @param delimiters        the delimiter characters, assembled as a {@code String}
   *                          (each of the characters is individually considered as a delimiter)
   * @param trimTokens        trim the tokens via {@link String#trim()}
   * @param ignoreEmptyTokens omit empty tokens from the result array
   *                          (only applies to tokens that are empty after trimming; StringTokenizer
   *                          will not consider subsequent delimiters as token in the first place).
   * @return an array of the tokens
   * @see java.util.StringTokenizer
   * @see String#trim()
   */
  public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

    if (str == null) {
      return EMPTY_STRING_ARRAY;
    }

    StringTokenizer st = new StringTokenizer(str, delimiters);
    List<String> tokens = new ArrayList<>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (trimTokens) {
        token = token.trim();
      }
      if (!ignoreEmptyTokens || token.length() > 0) {
        tokens.add(token);
      }
    }
    return toStringArray(tokens);
  }

  /**
   * Copy the given {@link Collection} into a {@code String} array.
   * <p>The {@code Collection} must contain {@code String} elements only.
   *
   * @param collection the {@code Collection} to copy
   *                   (potentially {@code null} or empty)
   * @return the resulting {@code String} array
   */
  public static String[] toStringArray(Collection<String> collection) {
    return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
  }

  /**
   * Check whether the given {@code String} contains actual <em>text</em>.
   * <p>More specifically, this method returns {@code true} if the
   * {@code String} is not {@code null}, its length is greater than 0,
   * and it contains at least one non-whitespace character.
   *
   * @param str the {@code String} to check (may be {@code null})
   * @return {@code true} if the {@code String} is not {@code null}, its
   * length is greater than 0, and it does not contain whitespace only
   * @see Character#isWhitespace
   */
  public static boolean hasText(String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  public static boolean isBlank(String str) {
    return !hasText(str);
  }

  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

}
