package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import java.util.function.Supplier;

import static org.folio.rest.RestVerticle.MODULE_SPECIFIC_ARGS;

public class DaoExecutor {
  private static final int THREAD_POOL_SIZE =
    Integer.parseInt(MODULE_SPECIFIC_ARGS.getOrDefault("file.processing.thread.pool.size", "20"));

  /* WorkerExecutor provides separate worker pool for code execution */
  private final static WorkerExecutor EXECUTOR =
    Vertx.vertx().createSharedWorkerExecutor("processing-files-thread-pool", THREAD_POOL_SIZE);

  public static <R> Future<R> executeBlocking(Supplier<R> supplier) {
    if (supplier == null) {
      throw new NullPointerException();
    } else {
      Future<R> future = Future.future();
      try {
        EXECUTOR.executeBlocking(blockingFuture -> {
          R returnValue = supplier.get();
          future.complete(returnValue);
          blockingFuture.complete();
        }, false, null);
      } catch (Throwable e) {
        future.fail(e);
      }
      return future;
    }
  }
}
