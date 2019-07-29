package org.folio.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.folio.dao.EventDao;
import org.folio.dao.QueryExecutor;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.Metadata;
import org.folio.rest.persist.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventDaoImpl implements EventDao {

  private static final String TABLE_NAME = "events";
  private final RowMapper MAPPER = (resultSet, rowNum) ->
    new Event()
      .withId(resultSet.getString("id"))
      .withEventType(resultSet.getString("eventType"))
      .withMetadata(new JsonObject(resultSet.getString("metadata")).mapTo(Metadata.class));
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public Future<Optional<Event>> getById(String eventId, String tenantId) {
    return QueryExecutor.executeBlocking(() -> {
      String query = new StringBuilder()
        .append("select * from ")
        .append(getTableName(tenantId))
        .append(" where id = ?")
        .toString();
      List<Event> events = jdbcTemplate.query(query, MAPPER, eventId);
      return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    });
  }

  private StringBuilder getTableName(String tenantId) {
    return new StringBuilder()
      .append(PostgresClient.convertToPsqlStandard(tenantId))
      .append(".")
      .append(TABLE_NAME);
  }


}
