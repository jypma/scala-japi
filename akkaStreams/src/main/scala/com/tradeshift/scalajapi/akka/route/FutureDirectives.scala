package com.tradeshift.scalajapi.akka.route

import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.function.{ Function ⇒ JFunction }

import java.util.function.Supplier

import scala.compat.java8.FutureConverters._
import scala.concurrent.ExecutionContext.Implicits.global // only to unwrap the CompletionException
import scala.util.Try

import akka.http.scaladsl.server.directives.{ FutureDirectives ⇒ D }

trait FutureDirectives {
  def onComplete[T](f: Supplier[CompletionStage[T]], inner: JFunction[Try[T], Route]): Route = ScalaRoute(
    D.onComplete(f.get.toScala.recover(unwrapCompletionException)) { value ⇒
      inner.apply(value).toScala
    })

  def onSuccess[T](f: Supplier[CompletionStage[T]], inner: JFunction[T, Route]): Route = ScalaRoute(
    D.onSuccess(f.get.toScala.recover(unwrapCompletionException)) { value ⇒
      inner.apply(value).toScala
    })

  // This might need to be raised as an issue to scala-java8-compat instead.
  // Right now, having this in Java:
  //     CompletableFuture.supplyAsync(() -> { throw new IllegalArgumentException("always failing"); })
  // will in fact fail the future with CompletionException.
  private def unwrapCompletionException[T]: PartialFunction[Throwable, T] = {
    case x: CompletionException if x.getCause ne null ⇒
      throw x.getCause
  }
}
