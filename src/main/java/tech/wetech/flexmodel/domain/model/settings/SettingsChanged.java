package tech.wetech.flexmodel.domain.model.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author cjbi
 */
@Getter
@AllArgsConstructor
@ToString
public class SettingsChanged {
  private Settings message;
}
