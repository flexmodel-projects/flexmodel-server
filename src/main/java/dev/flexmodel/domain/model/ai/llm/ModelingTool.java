package dev.flexmodel.domain.model.ai.llm;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.modeling.ModelService;
import dev.flexmodel.model.SchemaObject;

import java.util.List;

/**
 * Langchain4j tool to list available models in the system
 */
@ApplicationScoped
public class ModelingTool {

  @Inject
  ModelService modelService;

  @Inject
  DatasourceService datasourceService;

  @Tool("获取数据源列表")
  @NonBlocking
  public String listDatasources(@P("项目ID") String projectId) {
    List<Datasource> dataSources = datasourceService.findAll(projectId);
    if (dataSources.isEmpty()) {
      return "No data sources are currently available.";
    }

    StringBuilder response = new StringBuilder();
    response.append("Available data sources:\n\n");

    for (int i = 0; i < dataSources.size(); i++) {
      response.append((i + 1)).append(". ").append(dataSources.get(i).getName()).append("\n");
    }

    return response.toString();
  }

  @Tool("获取某个数据源中所有可用数据模型的列表")
  @NonBlocking
  public String listModelsByDatasource(@P("项目ID") String projectId, @P("数据源名称") String datasourceName) {
    if (datasourceName == null || datasourceName.trim().isEmpty()) {
      datasourceName = "dev_test"; // default datasource
    }

    try {
      List<SchemaObject> models = modelService.findAll(projectId, datasourceName.trim());

      if (models.isEmpty()) {
        return String.format("No models are currently available in the datasource '%s'.", datasourceName);
      }

      // Format the model list for AI response
      StringBuilder response = new StringBuilder();
      response.append(String.format("Available models in datasource '%s':\n\n", datasourceName));

      for (int i = 0; i < models.size(); i++) {
        SchemaObject model = models.get(i);
        response.append((i + 1)).append(". ").append(model.getName());

        // Add model type if available
        if (model.getType() != null) {
          response.append(" (Type: ").append(model.getType()).append(")");
        }

        response.append("\n");
      }

      return response.toString();
    } catch (Exception e) {
      return String.format("Error retrieving model list from datasource '%s': %s", datasourceName, e.getMessage());
    }
  }

  @Tool("获取某个数据源中某个数据模型详细信息")
  @NonBlocking
  public String getModelByName(@P("项目ID") String projectId, @P("数据源名称") String datasourceName, @P("模型名称") String modelName) {
    if (datasourceName == null || datasourceName.trim().isEmpty()) {
      datasourceName = "dev_test"; // default datasource
    }
    if (modelName == null || modelName.trim().isEmpty()) {
      return "No model name provided.";
    }
    try {
      return modelService.findModel(projectId, datasourceName.trim(), modelName.trim())
        .map(SchemaObject::getIdl)
        .orElse("No model exists");
    } catch (Exception e) {
      return String.format("Error retrieving model from datasource '%s': %s", datasourceName, e.getMessage());
    }
  }

  @NonBlocking
  @Tool("在指定数据源下面执行IDL语句")
  public String modelingByIdl(@P("项目ID") String projectId, @P("数据源名称") String datasourceName, @P("模型IDL") String idl) {
    if (datasourceName == null || datasourceName.trim().isEmpty()) {
      datasourceName = "dev_test"; // default datasource
    }
    String replacedIdl = idl.replaceAll("\n", "");
    try {
      modelService.importModels(projectId, datasourceName, replacedIdl, "idl");
      // success
      return "success";
    } catch (Exception e) {
      return String.format("Error retrieving model from datasource '%s': %s", datasourceName, e.getMessage());
    }
  }

}
