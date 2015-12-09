package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server
import java.util.function
import akka.http.scaladsl.server.Rejection
import scala.reflect.ClassTag
import com.tradeshift.scalajapi.collect.Seq

object RejectionHandler {
  def newBuilder = new RejectionHandlerBuilder(server.RejectionHandler.newBuilder)
  
  def defaultHandler = RejectionHandler(server.RejectionHandler.default)
}

case class RejectionHandler(unwrap: server.RejectionHandler) {
  def withFallback(fallback: RejectionHandler) = RejectionHandler(unwrap.withFallback(fallback.unwrap))
  
  def seal = RejectionHandler(unwrap.seal)
}

class RejectionHandlerBuilder(unwrap: server.RejectionHandler.Builder) {
  def build = RejectionHandler(unwrap.result())
  
  def handle[T <: Rejection](t: Class[T], handler: function.Function[T, Route]): RejectionHandlerBuilder = {
    unwrap.handle { case r if t.isInstance(r) => handler.apply(t.cast(r)).toScala }
    this
  }
  
  def handleAll[T <: Rejection](t: Class[T], handler: function.Function[Seq[T], Route]): RejectionHandlerBuilder = {
    unwrap.handleAll { rejections:collection.immutable.Seq[T] => handler.apply(Seq.wrap(rejections)).toScala } (ClassTag(t))
    this
  }
}
