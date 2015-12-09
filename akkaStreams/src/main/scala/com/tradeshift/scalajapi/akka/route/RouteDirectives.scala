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
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.javadsl.model.HttpHeader
import akka.http.javadsl.model.RequestEntity
import com.tradeshift.scalajapi.collect.Seq

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
  
  def complete[T](value: T, marshaller: Marshaller[T,HttpResponse]) = ScalaRoute {
    D.complete(ToResponseMarshallable(value)(marshaller.unwrap))
  }
  
  def complete[T](status: StatusCode, headers: Seq[HttpHeader], value: T, marshaller: Marshaller[T,RequestEntity]) = ScalaRoute {
    D.complete(ToResponseMarshallable(value)(fromToEntityMarshaller(status, headers.unwrap)(marshaller.unwrap)))
  }
  
  def complete[T](status: StatusCode, value: T, marshaller: Marshaller[T,RequestEntity]) = ScalaRoute {
    D.complete(ToResponseMarshallable(value)(fromToEntityMarshaller(status)(marshaller.unwrap)))
  }
  
  def complete[T](headers: Seq[HttpHeader], value: T, marshaller: Marshaller[T,RequestEntity]) = ScalaRoute {
    D.complete(ToResponseMarshallable(value)(fromToEntityMarshaller(headers = headers.unwrap)(marshaller.unwrap)))
  }
  
  def completeOK[T](value: T, marshaller: Marshaller[T,RequestEntity]) = ScalaRoute {
    D.complete(ToResponseMarshallable(value)(fromToEntityMarshaller()(marshaller.unwrap)))
  }
}
