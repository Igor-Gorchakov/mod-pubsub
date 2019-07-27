package org.folio.dao.impl;

import io.vertx.core.Future;
import org.folio.dao.DaoExecutor;
import org.folio.dao.EventDao;
import org.folio.rest.jaxrs.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.Optional;

@Repository
public class EventDaoImpl implements EventDao {

  @Autowired
  private JdbcTemplate jdbcTemplate;
  private RowMapper<Event> ROW_MAPPER = (ResultSet resultSet, int rowNum) ->
    new Event()
      .withId(resultSet.getString("id"))
      .withEventType(resultSet.getString("type"));

  @Override
  public Future<Optional<Event>> getById(String eventId, String tenantId) {
    return DaoExecutor.executeBlocking(() -> Optional.of(
      new Event()
//      jdbcTemplate.queryForObject("select * from events where id = ?", new Object[]{eventId}, ROW_MAPPER)
    ));
  }
}
