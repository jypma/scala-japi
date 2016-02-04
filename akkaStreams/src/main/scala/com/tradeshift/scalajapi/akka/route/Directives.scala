package com.tradeshift.scalajapi.akka.route

import scala.annotation.varargs
import akka.http.scaladsl.server.Rejection

object Directives extends AllDirectives {
  @varargs override def route(alternatives: Route*): Route = super.route(alternatives: _*)
  @varargs override def rejectWith(rejections: Rejection*): Route = super.rejectWith(rejections: _*) 
}
