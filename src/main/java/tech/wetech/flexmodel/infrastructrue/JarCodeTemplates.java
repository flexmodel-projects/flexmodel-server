package tech.wetech.flexmodel.infrastructrue;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.code_templates.Example;
import tech.wetech.flexmodel.domain.model.codegen.TemplateProvider;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author cjbi
 */
@Slf4j
@Dependent
@Startup
public class JarCodeTemplates implements TemplateProvider {

  private static final String TEMPLATE_ROOT = "templates";

  private static FileSystem fs = null;

  private static List<String> templateNames;

  public void installFileSystem(@Observes StartupEvent startupEvent) {
    URL resUrl = Example.class
      .getClassLoader()
      .getResource(TEMPLATE_ROOT);
    if (resUrl == null) {
      throw new IllegalStateException("资源未找到: " + TEMPLATE_ROOT);
    }
    Map<String, String> env = Map.of("create", "false");
    try {
      JarURLConnection jarCon = (JarURLConnection) resUrl.openConnection();
      URL jarFileUrl = jarCon.getJarFileURL();  // 获取实际 JAR 文件地址 :contentReference[oaicite:6]{index=6}
      Path jarPath = Paths.get(jarFileUrl.toURI());  // 转为本地 Path :contentReference[oaicite:7]{index=7}
      fs = FileSystems.newFileSystem(jarPath, env);
      Path tplPath = fs.getPath("/" + TEMPLATE_ROOT);
      try (Stream<Path> stream = Files.list(tplPath)) {
        templateNames = stream.map(p -> p.getFileName().toString())
          .toList();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          fs.close();
        } catch (IOException ignored) {
        }
      }));
      log.info("Jar package template has been mounted successfully");
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException("获取模板路径失败", e);
    }
  }

  @Override
  public Path getTemplatePath(String templateName) {
    return fs.getPath("/" + TEMPLATE_ROOT + "/" + templateName);
  }

  @Override
  public List<String> getTemplateNames() {
    return templateNames;
  }
}
