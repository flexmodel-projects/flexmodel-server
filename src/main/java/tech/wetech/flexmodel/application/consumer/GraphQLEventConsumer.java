package tech.wetech.flexmodel.application.consumer;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.GraphQLManger;
import tech.wetech.flexmodel.application.dto.GraphQLRefreshEvent;
import tech.wetech.flexmodel.codegen.entity.Datasource;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.graphql.FlexmodelGraphQL;
import tech.wetech.flexmodel.session.SessionFactory;

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

  @ConsumeEvent("graphql.refresh")
  public void consume(GraphQLRefreshEvent event) {
    long beginTime = System.currentTimeMillis();
    log.info("Received graphql message");
    List<Datasource> datasourceList = datasourceService.findAll();
    Map<String, List<String>> dsMap = datasourceList.stream()
      .collect(groupingBy(Datasource::getTenantId, mapping(Datasource::getName, toList())));
    FlexmodelGraphQL fg = new FlexmodelGraphQL();
    graphQLManger.addDefaultGraphQL(fg.generateGraphQLWithSchemaObject(sf, sf.getSchemaNames()));
    dsMap.forEach((tenantId, datasourceNames) -> graphQLManger.addGraphQL(tenantId, fg.generateGraphQLWithSchemaObject(sf, datasourceNames)));
    log.info("========== GraphQL init successful in {} ms!", System.currentTimeMillis() - beginTime);
  }

}
