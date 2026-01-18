package dev.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.NodeInstanceLog;
import dev.flexmodel.domain.model.flow.repository.NodeInstanceLogRepository;
import dev.flexmodel.session.Session;

import java.util.List;

@ApplicationScoped
public class NodeInstanceLogFmRepository implements NodeInstanceLogRepository {

  @Inject
  Session session;

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


