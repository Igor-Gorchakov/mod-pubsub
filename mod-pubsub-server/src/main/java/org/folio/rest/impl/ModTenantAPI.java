package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ModTenantAPI extends TenantAPI {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModTenantAPI.class);
  private static final String TENANT_PLACEHOLDER = "${myuniversity}";
  private static final String MODULE_PLACEHOLDER = "${mymodule}";
  private static final String INITIALIZE_TABLES_SQL = "templates/db_scripts/tables/initializeTables.sql";

  @Validate
  @Override
  public void postTenant(TenantAttributes entity, Map<String, String> headers, Handler<AsyncResult<Response>> handlers, Context context) {
    super.postTenant(entity, headers, ar -> {
      if (ar.failed()) {
        handlers.handle(ar);
      } else {
        executeQuery(INITIALIZE_TABLES_SQL, headers, context)
          .setHandler(event -> handlers.handle(ar));
      }
    }, context);
  }

  private Future<List<String>> executeQuery(String script, Map<String, String> headers, Context context) {
    try {
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(script);
      String sqlScript = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
      if (StringUtils.isBlank(sqlScript)) {
        return Future.succeededFuture();
      } else {
        Future<List<String>> future = Future.future();
        String tenantId = TenantTool.calculateTenantId(headers.get("x-okapi-tenant"));
        String moduleName = PostgresClient.getModuleName();
        sqlScript = sqlScript.replace(TENANT_PLACEHOLDER, tenantId).replace(MODULE_PLACEHOLDER, moduleName);
        PostgresClient.getInstance(context.owner()).runSQLFile(sqlScript, false, future);
        return future;
      }
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

}
