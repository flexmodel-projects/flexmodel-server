package dev.flexmodel.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 文件项
 * @author cjbi
 */
@Data
@Builder
public class FileItem {

  private String name;

  private FileType type;

  private Long size;

  private Instant lastModified;

  private String path;

  public enum FileType {
    file,
    folder
  }
}
