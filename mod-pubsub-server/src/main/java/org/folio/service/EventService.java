package org.folio.service;

import io.vertx.core.Future;

import java.util.Optional;

public interface EventService {

  Future<Optional> getEventById(String eventId, String tenantId);
}
