package dev.flexmodel.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author cjbi
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GenerateAPIsDTO {
  @NotBlank
  private String datasourceName;
  @NotBlank
  private String modelName;
  @NotBlank
  private String apiFolder;
  @NotBlank
  private String idFieldOfPath;
  @NotEmpty
  private List<String> generateAPIs;
}
