package tech.wetech.flexmodel.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author cjbi
 */
public class CollectionUtils {

  /**
   * Return {@code true} if the supplied Collection is {@code null} or empty.
   * Otherwise, return {@code false}.
   *
   * @param collection the Collection to check
   * @return whether the given Collection is empty
   */
  public static boolean isEmpty(Collection<?> collection) {
    return (collection == null || collection.isEmpty());
  }

  /**
   * Return {@code true} if the supplied Map is {@code null} or empty.
   * Otherwise, return {@code false}.
   *
   * @param map the Map to check
   * @return whether the given Map is empty
   */
  public static boolean isEmpty(Map<?, ?> map) {
    return (map == null || map.isEmpty());
  }
}

