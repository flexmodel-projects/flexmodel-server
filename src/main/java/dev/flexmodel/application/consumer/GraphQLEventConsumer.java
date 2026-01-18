package dev.flexmodel.application.consumer;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.Project;
import dev.flexmodel.domain.model.api.GraphQLManger;
import dev.flexmodel.application.dto.GraphQLRefreshEvent;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.auth.ProjectService;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.graphql.FlexmodelGraphQL;
import dev.flexmodel.session.SessionFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * 监听GraphQL变更事件
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class GraphQLEventConsumer {

  @Inject
  SessionFactory sf;
  @Inject
  DatasourceService datasourceService;
  @Inject
  GraphQLManger graphQLManger;
  @Inject
  ProjectService projectService;

  public void handle(@Observes StartupEvent startupEvent) {
    consume(new GraphQLRefreshEvent());
  }

  @ConsumeEvent("graphql.refresh")
  public void consume(GraphQLRefreshEvent event) {
    long beginTime = System.currentTimeMillis();
    log.info("Received graphql message");
    List<Project> projects = projectService.findProjects();
    for (Project project : projects) {
      List<Datasource> datasourceList = datasourceService.findAll(project.getId());
      Map<String, List<String>> dsMap = datasourceList.stream()
        .filter(f -> f.getProjectId() != null)
        .collect(groupingBy(Datasource::getProjectId, mapping(Datasource::getName, toList())));
      FlexmodelGraphQL fg = new FlexmodelGraphQL();
      graphQLManger.addDefaultGraphQL(fg.generateGraphQLWithSchemaObject(sf, sf.getSchemaNames()));
      dsMap.forEach((projectId, datasourceNames) -> graphQLManger.addGraphQL(projectId, fg.generateGraphQLWithSchemaObject(sf, datasourceNames)));
    }
    log.info("========== GraphQL init successful in {} ms!", System.currentTimeMillis() - beginTime);
  }

}
