package org.folio.rest.impl;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.UUID;

public abstract class AbstractRestTest {

  static final String TENANT_ID = "diku";
  static Vertx vertx;
  static RequestSpecification spec;

  @BeforeClass
  public static void setUpClass(final TestContext context) throws Exception {
    vertx = Vertx.vertx();
    runDatabase();
    deployVertical(context);
  }

  private static void runDatabase() throws Exception {
    String useExternalDatabase = System.getProperty(
      "org.folio.converter.storage.test.database",
      "embedded");

    switch (useExternalDatabase) {
      case "environment":
        System.out.println("Using environment settings");
        break;
      case "external":
        String postgresConfigPath = System.getProperty(
          "org.folio.converter.storage.test.config",
          "/postgres-conf-local.json");
        PostgresClient.setConfigFilePath(postgresConfigPath);
        break;
      case "embedded":
        PostgresClient.stopEmbeddedPostgres();
        PostgresClient.setIsEmbedded(true);
        PostgresClient.getInstance(vertx).startEmbeddedPostgres();
        break;
      default:
        String message = "No understood database choice made." +
          "Please set org.folio.converter.storage.test.database" +
          "to 'external', 'environment' or 'embedded'";
        throw new Exception(message);
    }
  }

  private static void deployVertical(TestContext context) {
    Async async = context.async();
    vertx = Vertx.vertx();
    int port = NetworkUtils.nextFreePort();
    String okapiUrl = "http://localhost:" + port;
    String okapiUserId = UUID.randomUUID().toString();

    TenantClient tenantClient = new TenantClient(okapiUrl, TENANT_ID, "dummy-token");
    DeploymentOptions restVerticleDeploymentOptions = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port));
    vertx.deployVerticle(RestVerticle.class.getName(), restVerticleDeploymentOptions, res -> {
      try {
        tenantClient.postTenant(null, res2 -> async.complete());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    spec = new RequestSpecBuilder()
      .setContentType(ContentType.JSON)
      .setBaseUri(okapiUrl)
      .addHeader(RestVerticle.OKAPI_HEADER_TENANT, TENANT_ID)
      .addHeader(RestVerticle.OKAPI_USERID_HEADER, okapiUserId)
      .build();
  }

  @AfterClass
  public static void tearDownClass(final TestContext context) {
    Async async = context.async();
    vertx.close(context.asyncAssertSuccess(res -> {
      PostgresClient.stopEmbeddedPostgres();
      async.complete();
    }));
  }
}
