package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server.directives.{ExecutionDirectives => D}

trait ExecutionDirectives {
  def handleExceptions(handler: ExceptionHandler, inner: java.util.function.Supplier[Route]) = ScalaRoute(
    D.handleExceptions(handler.unwrap) {
      inner.get.toScala
    }
  )
  
  def handleRejections(handler: RejectionHandler, inner: java.util.function.Supplier[Route]) = ScalaRoute(
    D.handleRejections(handler.unwrap) {
      inner.get.toScala
    }
  )
}
