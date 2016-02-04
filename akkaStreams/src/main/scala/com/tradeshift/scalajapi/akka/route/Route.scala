package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.concurrent.Future
import akka.stream.javadsl.Flow
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.scaladsl.server.{Route => SRoute}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.server.Directives._
import scala.annotation.varargs
import akka.NotUsed

trait Route {
  private[route] def toScala: SRoute
  
  def flow(system: ActorSystem, materializer: Materializer): Flow[HttpRequest, HttpResponse, NotUsed]
  def seal(system: ActorSystem, materializer: Materializer): Route
  def orElse(alternative: Route): Route
}
