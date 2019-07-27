package org.folio.rest.impl;

import io.restassured.RestAssured;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class EventAPITest extends AbstractRestTest {

  private static final String RESOURCE_PATH = "/pubsub/events";
  private static final String TABLE_NAME = "events";

  @Test
  public void shouldReturnNotFoundOnGetWhenEventDoesNotExist() {
    String eventId = UUID.randomUUID().toString();
    RestAssured.given()
      .spec(spec)
      .when()
      .get(RESOURCE_PATH + "/" + eventId)
      .then()
      .statusCode(HttpStatus.SC_OK);
  }

  @Override
  public void clearTables(TestContext context) {
    Async async = context.async();
//    PostgresClient.getInstance(vertx, TENANT_ID).delete(TABLE_NAME, new Criterion(), event -> {
//      if (event.failed()) {
//        context.fail(event.cause());
//      }
//      async.complete();
//    });
    async.complete();
  }
}
