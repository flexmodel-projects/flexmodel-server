package tech.wetech.flexmodel.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
@Slf4j
public class TemplatePathUtil {
  private static final String TEMPLATE_ROOT = "templates/java_template";
  // 缓存挂载的 FileSystem，避免重复打开
  private static final AtomicReference<FileSystem> zipFsRef = new AtomicReference<>();

  /**
   * 获取模板目录在当前运行环境下的 Path。
   * IDE/debug 模式：直接 file 协议读取；
   * JAR 模式：挂载 ZIP FileSystem，再获取子目录 Path。
   */
  public static Path getTemplatePath() {
    URL resUrl = TemplatePathUtil.class
      .getClassLoader()
      .getResource(TEMPLATE_ROOT);
    if (resUrl == null) {
      throw new IllegalStateException("资源未找到: " + TEMPLATE_ROOT);
    }

    String protocol = resUrl.getProtocol();  // file 或 jar :contentReference[oaicite:4]{index=4}

    try {
      if ("file".equalsIgnoreCase(protocol)) {
        // IDE 调试时，资源位于本地文件系统
        return Paths.get(resUrl.toURI());  // 转为 Path :contentReference[oaicite:5]{index=5}
      } else if ("jar".equalsIgnoreCase(protocol)) {
        // JAR 包中运行，先打开 JarURLConnection 拿到 JAR 文件 URL
        JarURLConnection jarCon = (JarURLConnection) resUrl.openConnection();
        URL jarFileUrl = jarCon.getJarFileURL();  // 获取实际 JAR 文件地址 :contentReference[oaicite:6]{index=6}
        Path jarPath = Paths.get(jarFileUrl.toURI());  // 转为本地 Path :contentReference[oaicite:7]{index=7}

        // 缓存并复用同一个 FileSystem
        FileSystem zipFs = zipFsRef.updateAndGet(existing -> {
          if (existing != null && existing.isOpen()) {
            return existing;
          }
          try {
            FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
            // JVM 退出时关闭
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
              try {
                fs.close();
              } catch (IOException ignored) {
              }
            }));
            return fs;
          } catch (IOException e) {
            throw new RuntimeException("无法挂载 JAR 文件系统: " + jarPath, e);
          }
        });
        // 返回模板目录的 Path
        return zipFs.getPath("/" + TEMPLATE_ROOT);  // 无需层次化 URI :contentReference[oaicite:8]{index=8}
      } else {
        throw new UnsupportedOperationException("不支持的协议: " + protocol);
      }
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException("获取模板路径失败", e);
    }
  }

  /**
   * 示例：遍历模板目录文件
   */
  public static void printTemplateFiles() {
    Path tplDir = getTemplatePath();
    try (Stream<Path> paths = Files.walk(tplDir)) {
      paths.forEach(System.out::println);
    } catch (IOException e) {
      throw new RuntimeException("遍历模板目录失败", e);
    }
  }
}
