package org.folio.rest.impl;

import io.restassured.RestAssured;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    String url = "/pubsub/module-info";
    RestAssured.given()
      .spec(spec)
      .when()
      .get(url)
      .then()
      .statusCode(HttpStatus.SC_OK);
  }
}
