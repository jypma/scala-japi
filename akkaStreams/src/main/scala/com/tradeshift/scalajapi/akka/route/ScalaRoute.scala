package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.concurrent.Future
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.scaladsl.Flow
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.server.{Route => SRoute}
import akka.http.scaladsl
import akka.NotUsed


case class ScalaRoute(toScala: akka.http.scaladsl.server.Route) extends Route  {
  override def flow(system: ActorSystem, materializer: Materializer) = scalaFlow(system,materializer).asJava
  
  private def scalaFlow(system: ActorSystem, materializer: Materializer): Flow[HttpRequest, HttpResponse, NotUsed] = {
    implicit val s = system
    implicit val m = materializer
    SRoute.handlerFlow(toScala)
  }
  
  override def orElse(alternative: Route): Route = alternative match {
    case ScalaRoute(altRoute) =>
      ScalaRoute(toScala ~ altRoute)
      
    case _ => throw new IllegalArgumentException("TODO wrap java route in scala")
  }
  
  override def seal(system: ActorSystem, materializer: Materializer): Route = {
    implicit val s = system
    implicit val m = materializer

    ScalaRoute(SRoute.seal(toScala))
  }
}
