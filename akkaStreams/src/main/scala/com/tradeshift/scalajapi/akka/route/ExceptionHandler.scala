package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server

object ExceptionHandler {
  def of(pf: PartialFunction[Throwable, Route]) = ExceptionHandler(server.ExceptionHandler(pf.andThen(_.toScala)))
}

case class ExceptionHandler(unwrap: server.ExceptionHandler) {
  
}
