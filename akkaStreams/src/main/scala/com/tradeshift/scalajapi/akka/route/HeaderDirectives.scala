package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server.directives.{HeaderDirectives => D}
import akka.http.javadsl.model.HttpHeader
import java.util.{function => jf}
import scala.reflect.ClassTag
import akka.http.scaladsl.server.util.ClassMagnet
import com.tradeshift.scalajapi.collect.Option

/**
 * (copy / refer to scala DSL doc here)
 * 
 * When implementing custom headers in Java, just extend akka.http.scaladsl.model.headers.CustomHeader,
 * since the java API's CustomHeader class is pretty cumbersome to use.
 */
trait HeaderDirectives {
  def headerValue[T](f: jf.Function[HttpHeader, Option[T]], inner: jf.Function[T, Route]) = ScalaRoute {
    D.headerValue(h => f.apply(h).unwrap) { value => 
      inner.apply(value).toScala
    }
  }
  
  def headerValuePF[T](pf: PartialFunction[HttpHeader, T], inner: jf.Function[T, Route]) = ScalaRoute {
    D.headerValuePF(pf) { value =>
      inner.apply(value).toScala
    }
  }
  
  def headerValueByName(headerName: String, inner: jf.Function[String, Route]) = ScalaRoute {
    D.headerValueByName(headerName) { value =>
      inner.apply(value).toScala
    }
  }
  
  def headerValueByType[T <: HttpHeader](t: Class[T], inner: jf.Function[T, Route]) = ScalaRoute {
    D.headerValueByType(ClassMagnet(t).asInstanceOf[ClassMagnet[akka.http.scaladsl.model.HttpHeader]]) { value =>
      inner.apply(value.asInstanceOf[T]).toScala
    }
  }
  
  def optionalHeaderValue[T](f: jf.Function[HttpHeader, Option[T]], inner: jf.Function[Option[T], Route]) = ScalaRoute {
    D.optionalHeaderValue(h => f.apply(h).unwrap) { value => 
      inner.apply(Option.wrap(value)).toScala
    }
  }
  
  def optionalHeaderValuePF[T](pf: PartialFunction[HttpHeader, T], inner: jf.Function[Option[T], Route]) = ScalaRoute {
    D.optionalHeaderValuePF(pf) { value =>
      inner.apply(Option.wrap(value)).toScala
    }
  }
  
  def optionalHeaderValueByName(headerName: String, inner: jf.Function[Option[String], Route]) = ScalaRoute {
    D.optionalHeaderValueByName(headerName) { value =>
      inner.apply(Option.wrap(value)).toScala
    }
  }
  
  def optionalHeaderValueByType[T <: HttpHeader](t: Class[T], inner: jf.Function[Option[T], Route]) = ScalaRoute {
    D.optionalHeaderValueByType(ClassMagnet(t).asInstanceOf[ClassMagnet[akka.http.scaladsl.model.HttpHeader]]) { value =>
      inner.apply(value.asInstanceOf[Option[T]]).toScala
    }
  }
  
}
