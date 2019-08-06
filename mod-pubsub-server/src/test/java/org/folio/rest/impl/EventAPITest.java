package org.folio.rest.impl;

import io.restassured.RestAssured;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.dao.util.LiquibaseUtil;
import org.folio.rest.tools.utils.Envs;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(VertxUnitRunner.class)
public class EventAPITest extends AbstractRestTest {

  private static final String RESOURCE_PATH = "/pubsub/events";

  @Test
  public void shouldReturnEventOnGetById() {
    String eventId = "f14bb724-15bd-4a20-81f7-b698094160d0";
    RestAssured.given()
      .spec(spec)
      .when()
      .get(RESOURCE_PATH + "/" + eventId)
      .then()
      .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void shouldReturnModuleConfig() throws SQLException {
    try (Connection connection = LiquibaseUtil.getConnectionForModule()) {
      ResultSet resultSet = connection.createStatement().executeQuery("select module, events from pubsub_config.module_events limit 1");
      Assert.assertTrue(resultSet.next());
      Assert.assertEquals("test", resultSet.getString("module"));
      Assert.assertEquals("list of events", resultSet.getString("events"));
    }
  }
}
