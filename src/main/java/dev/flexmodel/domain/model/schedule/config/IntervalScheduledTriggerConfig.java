package dev.flexmodel.domain.model.schedule.config;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.domain.model.schedule.TriggerException;

/**
 * 间隔定时触发器配置类
 * 基于固定间隔时间的定时触发器配置
 *
 * @author cjbi
 */
@Getter
@Setter
public class IntervalScheduledTriggerConfig extends ScheduledTriggerConfig {

  /**
   * 间隔时间数值
   * 指定间隔的具体数值
   */
  private Integer interval;

  /**
   * 间隔时间单位
   * 指定间隔时间的单位，如：秒、分钟、小时、天等
   * second | minute | hour | day | week | month | year
   */
  private String intervalUnit;

  /**
   * 重复执行次数
   * 指定触发器重复执行的次数，默认为1次
   */
  private Integer repeatCount = 1;

  /**
   * 获取触发器来源
   * @return 触发器来源 "interval"
   */
  @Override
  public String type() {
    return "interval";
  }

  @Override
  public void validate() {
    if (interval == null || intervalUnit == null) {
      throw new TriggerException("IntervalScheduledTriggerConfig is invalid.");
    }
    if (interval <= 0) {
      throw new TriggerException("IntervalScheduledTriggerConfig.interval must be greater than 0.");
    }
  }
}
