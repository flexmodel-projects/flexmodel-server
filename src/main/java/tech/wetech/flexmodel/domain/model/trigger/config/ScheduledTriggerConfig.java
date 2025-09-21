package tech.wetech.flexmodel.domain.model.trigger.config;

/**
 * 定时触发器配置抽象类
 * 所有定时触发器的基类，定义了定时触发器的通用行为
 * 
 * @author cjbi
 */
public abstract class ScheduledTriggerConfig implements TriggerConfig {
  
  /**
   * 获取触发器类型
   * 定时触发器统一返回 "SCHEDULED"
   * 
   * @return 触发器类型 "SCHEDULED"
   */
  @Override
  public String type() {
    return "SCHEDULED";
  }
}
