package tech.wetech.flexmodel.domain.model.codegen;

import groovy.lang.GroovyClassLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.codegen.EnumClass;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;
import tech.wetech.flexmodel.util.JsonUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static tech.wetech.flexmodel.util.StringUtils.simpleRenderTemplate;

@Slf4j
@ApplicationScoped
public class CodeGenerationService {

  @Inject
  ModelService modelService;

  GroovyClassLoader loader = new GroovyClassLoader();

  /**
   * 根据 datasource 和 modelName，生成代码到临时目录并返回根路径。
   */
  public Path generateCode(String datasourceName) {
    List<File> outputFiles = new ArrayList<>();
    try {
      GenerationContext ctx = buildContext(datasourceName);
      java.nio.file.Path targetPath = Paths.get(System.getProperty("java.io.tmpdir"), "codegen", "" + System.currentTimeMillis());
      File templateDir = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("templates/java_template/")).toURI());
      outputFiles(loader, ctx, new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("templates/")).toURI()),
        templateDir.getAbsolutePath(), targetPath.toString(), outputFiles);
      return targetPath;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void outputFiles(GroovyClassLoader classLoader, GenerationContext context, File dir, String sourceDirectory, String targetDirectory, List<File> outputFiles) throws Exception {
    File[] files = dir.listFiles();

    for (File file : files) {
      try {
        if (file.isDirectory()) {
          String filePath = simpleRenderTemplate(file.getAbsolutePath(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
          String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
            .replace("\\", "/");
          File targetDir = new File(targetPath);
          targetDir.mkdirs();
          outputFiles.add(targetDir);
          outputFiles(classLoader, context, file, sourceDirectory, targetDirectory, outputFiles); // 递归遍历子目录
        } else {
          if (file.getName().endsWith(".groovy")) {
            Class<?> scriptClass = classLoader.parseClass(file);
            Object groovyObject = scriptClass.getDeclaredConstructor().newInstance();
            String filePath = simpleRenderTemplate(file.getParentFile().getAbsolutePath(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
            String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
              .replace("\\", "/");
            // 4. 调用 run(Map) 方法
            List<File> result = (List<File>) scriptClass.getMethod("generate", GenerationContext.class, String.class).invoke(groovyObject, context, targetPath);
            outputFiles.addAll(result);
          } else {
            String filePath = simpleRenderTemplate(file.getAbsolutePath(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
            String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
              .replace("\\", "/");
            Files.copy(file.toPath(), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            outputFiles.add(new File(targetPath));
          }
        }
      } catch (Exception e) {
        System.err.println("Generate file error, file:" + file);
        throw e;
      }
    }

  }

  private GenerationContext buildContext(String datasource) {
    GenerationContext ctx = new GenerationContext();
    ctx.setSchemaName(datasource);
    ctx.setPackageName("com.example");  // 可再配置化
    // 加载所有模型
    List<SchemaObject> models = modelService.findModels(datasource);
    models.forEach(m -> {
      if (m instanceof Entity) {
        ctx.getModelClassList()
          .add(ModelClass.buildModelClass("^fs_", ctx.getPackageName(), datasource, (Entity) m));
      } else {
        ctx.getEnumClassList()
          .add(EnumClass.buildEnumClass(ctx.getPackageName(), datasource, (Enum) m));
      }
    });
    return ctx;
  }

}
