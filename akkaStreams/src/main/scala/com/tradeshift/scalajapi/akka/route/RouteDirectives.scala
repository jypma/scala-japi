package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server.directives.{RouteDirectives => D}
import akka.http.scaladsl.server.directives._
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.server.Rejection
import scala.annotation.varargs
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl
import akka.http.scaladsl.marshalling.ToResponseMarshallable

trait RouteDirectives {
  /**
   * Java-specific call added so you can chain together multiple alternate routes using comma,
   * rather than having to explicitly call route1.orElse(route2).orElse(route3).
   */
  @varargs def route(alternatives: Route*) = ScalaRoute(
    alternatives.map(_.toScala).reduce(_ ~ _)
  )
  
  def reject: Route = ScalaRoute(
    D.reject
  )
  
  @varargs def rejectWith(rejections: Rejection*): Route = ScalaRoute(
    D.reject(rejections: _*)
  )
  
  def complete(body: String): Route = ScalaRoute(
    D.complete(body)
  )
  
  def complete(response: HttpResponse): Route = ScalaRoute(
    D.complete(response: scaladsl.model.HttpResponse)
  )
  
  def complete(status: StatusCode): Route = ScalaRoute(
    D.complete(status: scaladsl.model.StatusCode)
  )
  
  def complete[T](value: T, marshaller: Marshaller[T,HttpResponse]) = {
    ScalaRoute(
      D.complete(ToResponseMarshallable(value)(marshaller.unwrap))
    )
  }
}
