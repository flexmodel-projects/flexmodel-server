package dev.flexmodel.domain.model.connect;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@AllArgsConstructor
@Getter
public class NativeQueryResult {
  private long time;
  private Object result;
}
