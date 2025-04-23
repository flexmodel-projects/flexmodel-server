package tech.wetech.flexmodel.domain.model.codegen;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ZipService {

  /**
   * 将目录下所有文件打包为 ZIP，写入 outputStream。
   */
  public void zipDirectory(Path sourceDir, OutputStream outputStream) throws IOException {
    try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
      Files.walk(sourceDir)
        .forEach(path -> {
          Path rel = sourceDir.relativize(path);
          String entryName = "simple/" + rel.toString().replace(File.separatorChar, '/')
                             + (Files.isDirectory(path) ? "/" : "")
                               .replace("//", "/");
          try {
            zipOut.putNextEntry(new ZipEntry(entryName));
            if (Files.isRegularFile(path)) {
              Files.copy(path, zipOut);
            }
            zipOut.closeEntry();
          } catch (IOException e) {
            log.error("打包文件出错: {}", path, e);
          }
        });
      zipOut.finish();
    }
  }

}
