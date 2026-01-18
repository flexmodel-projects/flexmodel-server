package dev.flexmodel.application.dto;

import java.util.List;

/**
 * @author cjbi
 */
public record PageDTO<T>(List<T> list, Long total) {

  public static <T> PageDTO<T> empty() {
    return new PageDTO<>(List.of(), 0L);
  }

}
