package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.NodeInstanceLog;
import tech.wetech.flexmodel.domain.model.flow.repository.NodeInstanceLogRepository;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

@ApplicationScoped
public class NodeInstanceLogFmRepository implements NodeInstanceLogRepository {

  @Inject
  Session session;

  @Override
  public int insert(NodeInstanceLog nodeInstanceLog) {
    return session.dsl().insertInto(NodeInstanceLog.class).values(nodeInstanceLog).execute();
  }

  @Override
  public boolean insertList(List<NodeInstanceLog> nodeInstanceLogList) {
    boolean ok = true;
    for (NodeInstanceLog log : nodeInstanceLogList) {
      int r = session.dsl().insertInto(NodeInstanceLog.class).values(log).execute();
      ok &= r > 0;
    }
    return ok;
  }
}


