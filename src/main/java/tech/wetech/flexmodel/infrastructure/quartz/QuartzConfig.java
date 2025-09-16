package tech.wetech.flexmodel.infrastructure.quartz;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * @author cjbi
 */
@ApplicationScoped
public class QuartzConfig {

  @Produces
  public Scheduler scheduler() throws Exception {
    Properties props = new Properties();

    // 基础配置
    props.setProperty("org.quartz.scheduler.instanceName", "FlexmodelScheduler");
    props.setProperty("org.quartz.threadPool.threadCount", "5");

    // 使用自定义 JobStore
    props.setProperty("org.quartz.jobStore.class", FmJobStore.class.getName());
//    props.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());

    SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
    Scheduler scheduler = schedulerFactory.getScheduler();
    scheduler.start();
    return scheduler;
  }

}
