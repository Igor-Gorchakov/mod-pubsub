package org.folio.dao.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.folio.rest.persist.PostgresClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Util class to work with Liquibase nature
 */
public class LiquibaseUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseUtil.class);
  private static final String CHANGELOG_MODULE_PATH = "src/main/resources/liquibase/module/changelog.xml";
  private static final String CHANGELOG_TENANT_PATH = "src/main/resources/liquibase/tenant/changelog.xml";
  private static final String MODULE_SCHEMA = "pubsub_config";

  private static final String JDBC_DRIVER = "jdbc:postgresql";
  private static final String CONFIG_USERNAME_KEY = "username";
  private static final String CONFIG_PASSWORD_KEY = "password";
  private static final String CONFIG_HOST_KEY = "host";
  private static final String CONFIG_PORT_KEY = "port";
  private static final String CONFIG_DATABASE_KEY = "database";

  /**
   * Runs Liquibase scripts in a separate thread to initialize database for a given tenant
   *
   * @param tenant given tenant
   * @return future
   */
  public static Future<Void> initializeDatabaseForTenant(String tenant) {
    Future<Void> future = Future.future();
    LOGGER.info("Initializing database for tenant " + tenant);
    try (Connection connection = getConnectionForTenant(tenant)) {
      String schemaName = PostgresClient.convertToPsqlStandard(tenant);
      runScripts(schemaName, connection, CHANGELOG_TENANT_PATH);
      LOGGER.info("Database is initialized for tenant " + tenant);
      future.complete();
    } catch (Exception e) {
      String errorMessage = "Error while database initialization. Cause: " + e.getCause();
      LOGGER.error(errorMessage);
      future.fail(errorMessage);
    }
    return future;
  }

  private static Connection getConnectionForTenant(String tenant) throws SQLException {
    JsonObject connectionConfig = PostgresClient.getInstance(Vertx.vertx().getOrCreateContext().owner(), tenant).getConnectionConfig();
    return getConnectionInternal(connectionConfig);
  }

  public static Connection getConnectionForModule() throws SQLException {
    JsonObject connectionConfig = PostgresClient.getInstance(Vertx.vertx().getOrCreateContext().owner()).getConnectionConfig();
    return getConnectionInternal(connectionConfig);
  }

  /**
   * Obtains database connection for given tenant
   *
   * @return database connection
   * @throws SQLException if database access error occurred
   */
  private static Connection getConnectionInternal(JsonObject connectionConfig) throws SQLException {
    String username = connectionConfig.getString(CONFIG_USERNAME_KEY);
    String password = connectionConfig.getString(CONFIG_PASSWORD_KEY);
    String host = connectionConfig.getString(CONFIG_HOST_KEY);
    String port = String.valueOf(connectionConfig.getInteger(CONFIG_PORT_KEY));
    String database = connectionConfig.getString(CONFIG_DATABASE_KEY);
    String connectionUrl = String.format("%s://%s:%s/%s", JDBC_DRIVER, host, port, database);
    return DriverManager.getConnection(connectionUrl, username, password);
  }

  /**
   * Runs scripts for a given tenant
   * Runs scripts for a given tenant
   *
   * @param schemaName    tenant
   * @param connection    database connection
   * @param changelogPath
   * @throws LiquibaseException if scripts run is failed
   */
  private static void runScripts(String schemaName, Connection connection, String changelogPath) throws LiquibaseException {
    Liquibase liquibase = null;
    try {
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
      database.setDefaultSchemaName(schemaName);
      liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(), database);
      liquibase.update(new Contexts());
    } finally {
      if (liquibase != null && liquibase.getDatabase() != null) {
        Database database = liquibase.getDatabase();
        database.close();
      }
    }
  }

  /**
   * @return
   */
  public static Future<Void> initializeDatabaseForModule() {
    Future<Void> future = Future.future();
    try (Connection connection = getConnectionForModule()) {
      createSchema(MODULE_SCHEMA, connection);
      runScripts(MODULE_SCHEMA, connection, CHANGELOG_MODULE_PATH);
      LOGGER.info("Connection is OK!");
      future.complete();
    } catch (Exception e) {
      LOGGER.error(e);
      future.fail(e);
    }
    return future;
  }

  /**
   * @param schemaName
   * @param connection
   * @throws SQLException
   */
  private static void createSchema(String schemaName, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      String sql = "create schema if not exists " + schemaName;
      statement.executeUpdate(sql);
    }
  }
}
