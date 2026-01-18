package dev.flexmodel.domain.model.schedule.config;

/**
 * 触发器配置接口
 * 定义了触发器的基本属性和行为
 *
 * @author cjbi
 */
public interface TriggerConfig {

  /**
   * 获取触发器来源
   * @return 触发器来源字符串
   */
  String type();

  void validate();

}
