package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.dao.util.LiquibaseUtil;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.ModuleInfo;
import org.folio.rest.jaxrs.resource.Pubsub;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.service.EventService;
import org.folio.spring.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;

public class PubSubImpl implements Pubsub {
  private static final Logger LOGGER = LoggerFactory.getLogger(PubSubImpl.class);

  @Autowired
  private EventService eventService;
  private String tenantId;

  public PubSubImpl(Vertx vertx, String tenantId) { //NOSONAR
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
    this.tenantId = TenantTool.calculateTenantId(tenantId);
  }

  @Override
  public void getPubsubEventsById(String id, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      eventService.getEventById(id, tenantId).setHandler(ar -> {
        Response response;
        if (ar.succeeded()) {
          Optional<Event> optionalEvent = ar.result();
          if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            response = GetPubsubEventsByIdResponse.respond200WithApplicationJson(event);
          } else {
            String errorMessage = "Can not find Event with id: " + id;
            LOGGER.error(errorMessage);
            response = GetPubsubEventsByIdResponse.respond404WithTextPlain(errorMessage);
          }
        } else {
          String errorMessage = "Failed to get Event by id. Cause: " + ar.cause().getLocalizedMessage();
          LOGGER.error(errorMessage, ar.cause());
          response = GetPubsubEventsByIdResponse.respond500WithTextPlain(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }
        asyncResultHandler.handle(Future.succeededFuture(response));
      });
    } catch (Exception e) {
      LOGGER.error("Exception while getting Event by id. Cause: " + e.getLocalizedMessage(), e);
      asyncResultHandler.handle(Future.succeededFuture(
        GetPubsubEventsByIdResponse.respond500WithTextPlain(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())));
    }
  }

  @Override
  public void getPubsubModuleInfo(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try (Connection connection = LiquibaseUtil.getConnectionForModule()) {
      ResultSet resultSet = connection.createStatement().executeQuery("select module, events from pubsub_config.module_events limit 1");
      Response response;
      if (resultSet.next()) {
        String module = resultSet.getString("module");
        String events = resultSet.getString("events");
        ModuleInfo moduleInfo = new ModuleInfo()
          .withKey(module)
          .withValue(events);
        response = GetPubsubModuleInfoResponse.respond200WithApplicationJson(moduleInfo);
      } else {
        String errorMessage = "Can not find module information";
        LOGGER.error(errorMessage);
        response = GetPubsubModuleInfoResponse.respond404WithTextPlain(errorMessage);
      }
      asyncResultHandler.handle(Future.succeededFuture(response));
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        GetPubsubModuleInfoResponse.respond500WithTextPlain(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())));
    }
  }
}
