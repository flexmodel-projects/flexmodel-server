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
import tech.wetech.flexmodel.util.TemplatePathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
      Path templateDir = TemplatePathUtil.getTemplatePath();
      outputFiles(loader, ctx, templateDir,
        templateDir.toString(), targetPath.toString(), outputFiles);
      return targetPath;
    } catch (Exception e) {
      log.error("Generate code error", e);
      throw new RuntimeException(e);
    }
  }

  private void outputFiles(GroovyClassLoader classLoader, GenerationContext context, Path dir, String sourceDirectory, String targetDirectory, List<File> outputFiles) throws Exception {
    try (Stream<Path> paths = Files.walk(dir)) {
      paths.forEach(path -> {
        try {
          outFile(classLoader, context, sourceDirectory, targetDirectory, outputFiles, path);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("遍历模板目录失败", e);
    }

  }

  private static void outFile(GroovyClassLoader classLoader, GenerationContext context, String sourceDirectory, String targetDirectory, List<File> outputFiles, Path path) throws Exception {
    try {
      if (Files.isDirectory(path)) {
        String filePath = simpleRenderTemplate(path.toString(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
        String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
          .replace("\\", "/");
        File targetDir = new File(targetPath);
        targetDir.mkdirs();
        outputFiles.add(targetDir);
      } else {
        if (path.toString().endsWith(".groovy")) {
          Class<?> scriptClass = classLoader.parseClass(Files.readString(path));
          Object groovyObject = scriptClass.getDeclaredConstructor().newInstance();
          String filePath = simpleRenderTemplate(path.getParent().toString(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
          String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
            .replace("\\", "/");
          // 4. 调用 run(Map) 方法
          List<File> result = (List<File>) scriptClass.getMethod("generate", GenerationContext.class, String.class).invoke(groovyObject, context, targetPath);
          outputFiles.addAll(result);
        } else {
          String filePath = simpleRenderTemplate(path.toString(), JsonUtils.getInstance().convertValue(context, Map.class)).replace("\\", "/");
          String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
            .replace("\\", "/");
          Files.copy(path, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
          outputFiles.add(new File(targetPath));
        }
      }
    } catch (Exception e) {
      System.err.println("Generate file error, file:" + path);
      throw e;
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
