package com.tradeshift.scalajapi.akka.route

import akka.http.javadsl.model.HttpRequest
import akka.http.scaladsl.server.directives.{MarshallingDirectives => D}
import akka.http.javadsl.model.HttpEntity

trait MarshallingDirectives {
  def entityAs[T](unmarshaller: Unmarshaller[_ >: HttpRequest,T], 
                  inner: java.util.function.Function[T,Route]): Route = {
    ScalaRoute(
      D.entity(unmarshaller.unwrap) { value => 
        inner.apply(value).toScala
      }
    )    
  }
  
  def entityAs[T](t: Class[T], 
                  unmarshaller: Unmarshaller[_ >: HttpRequest,_ <: T], 
                  inner: java.util.function.Function[T,Route]): Route = {
    ScalaRoute(
      D.entity(unmarshaller.unwrap) { value => 
        inner.apply(value).toScala
      }
    )
  }
}
