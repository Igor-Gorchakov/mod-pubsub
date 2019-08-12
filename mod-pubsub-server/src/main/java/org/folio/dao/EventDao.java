package org.folio.dao;

import io.vertx.core.Future;

public interface EventDao {

  Future getById(String eventId, String tenantId);
}
