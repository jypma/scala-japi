package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server.directives.{MethodDirectives => D}
import java.util.function
import akka.http.javadsl.model.HttpMethod
import akka.http.scaladsl

trait MethodDirectives {
  def delete(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.delete { inner.get.toScala }
  )
  
  def get(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.get { inner.get.toScala }
  )
  
  def head(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.head { inner.get.toScala }
  )
  
  def options(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.options { inner.get.toScala }
  )
  
  def patch(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.patch { inner.get.toScala }
  )
  
  def post(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.post { inner.get.toScala }
  )
  
  def put(inner: function.Supplier[Route]): Route = ScalaRoute(
    D.put { inner.get.toScala }
  )
  
  def extractMethod(inner: function.Function[HttpMethod,Route]) = ScalaRoute (
    D.extractMethod { m =>
      inner.apply(m).toScala
    }
  )
  
  def method(method: HttpMethod, inner: function.Supplier[Route]): Route = ScalaRoute(
    D.method(method) { 
      inner.get.toScala 
    }
  )
}
