package com.tradeshift.scalajapi.akka.route

import akka.http.javadsl.model.HttpRequest

case class RequestContext(toScala: akka.http.scaladsl.server.RequestContext) {
  def getRequest: HttpRequest = toScala.request
}
