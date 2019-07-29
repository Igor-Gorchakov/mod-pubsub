package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import java.util.function.Supplier;

import static org.folio.rest.RestVerticle.MODULE_SPECIFIC_ARGS;

public class QueryExecutor {
  private static final int THREAD_POOL_SIZE =
    Integer.parseInt(MODULE_SPECIFIC_ARGS.getOrDefault("query.executor.thread.pool.size", "20"));
  /* WorkerExecutor provides separate worker pool for code execution */
  private final static WorkerExecutor EXECUTOR =
    Vertx.currentContext().owner().createSharedWorkerExecutor("query-executor", THREAD_POOL_SIZE);

  public static <T> Future<T> executeBlocking(Supplier<T> supplier) {
    if (supplier == null) {
      throw new NullPointerException();
    } else {
      Future<T> future = Future.future();
      EXECUTOR.executeBlocking(blockingFuture -> {
        try {
          T returnValue = supplier.get();
          future.complete(returnValue);
          blockingFuture.complete();
        } catch (Throwable e) {
          future.fail(e);
          blockingFuture.failed();
        }
      }, false, null);
      return future;
    }
  }
}
