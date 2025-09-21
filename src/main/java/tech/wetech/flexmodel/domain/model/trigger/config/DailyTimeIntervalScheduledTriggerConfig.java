package tech.wetech.flexmodel.domain.model.trigger.config;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.domain.model.trigger.TriggerException;
import tech.wetech.flexmodel.shared.utils.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 每日时间间隔定时触发器配置类
 * 在指定时间段内按间隔时间执行的定时触发器配置
 *
 * @author cjbi
 */
@Getter
@Setter
public class DailyTimeIntervalScheduledTriggerConfig extends ScheduledTriggerConfig {

  /**
   * 开始时间
   * 指定每日执行的开始时间
   */
  private LocalDateTime startTime;

  /**
   * 结束时间
   * 指定每日执行的结束时间
   */
  private LocalDateTime endTime;

  /**
   * 间隔时间数值
   * 指定在开始时间和结束时间之间的执行间隔
   */
  private Integer interval;

  /**
   * 间隔时间单位
   * 指定间隔时间的单位，如：秒、分钟、小时等
   * second | minute | hour
   */
  private String intervalUnit;

  /**
   * 星期几列表
   * 指定在哪些星期几执行，1-7分别代表周一到周日
   */
  private Set<Integer> daysOfWeek;

  /**
   * 获取触发器来源
   * @return 触发器来源 "daily_time_interval"
   */
  @Override
  public String from() {
    return "daily_time_interval";
  }

  @Override
  public void validate() {
    if (startTime == null || endTime == null || interval == null || intervalUnit == null || CollectionUtils.isEmpty(daysOfWeek)) {
      throw new TriggerException("DailyTimeIntervalScheduledTriggerConfig is invalid.");
    }
  }
}
