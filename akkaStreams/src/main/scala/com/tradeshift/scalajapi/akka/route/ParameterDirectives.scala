package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.collect.Option
import com.tradeshift.scalajapi.collect.Seq
import akka.http.scaladsl.server.directives.{ParameterDirectives => D}
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.server.directives.ParameterDirectives._
import akka.http.scaladsl.common.ToNameReceptacleEnhancements.string2NR

trait ParameterDirectives {
  def param(name: String, inner: java.util.function.Function[String,Route]): Route = ScalaRoute(
    D.parameter(name) { value =>
      inner.apply(value).toScala
    }
  )
  
  def paramOption(name: String, inner: java.util.function.Function[Option[String],Route]): Route = ScalaRoute(
    D.parameter(name.?) { value =>
      inner.apply(Option.wrap(value)).toScala
    }
  )
    
  def paramSeq(name: String, inner: java.util.function.Function[Seq[String],Route]): Route = ScalaRoute(
    D.parameter(string2NR(name).*) { values =>
      inner.apply(Seq.wrap(values.toVector)).toScala
    }
  )
  
  def param[T](t:Unmarshaller[String,T], name: String, inner: java.util.function.Function[T,Route]): Route = {
    import t.unwrap
    ScalaRoute(
      D.parameter(name.as[T]) { value =>
        inner.apply(value).toScala
      }
    )
  }
  
  def paramOption[T](t:Unmarshaller[String,T], name: String, inner: java.util.function.Function[Option[T],Route]): Route = {
    import t.unwrap
    ScalaRoute(
      D.parameter(name.as[T].?) { value =>
        inner.apply(Option.wrap(value)).toScala
      }
    )
  }
  
  def paramSeq[T](t:Unmarshaller[String,T], name: String, inner: java.util.function.Function[Seq[T],Route]): Route = {
    import t.unwrap
    ScalaRoute(
      D.parameter(name.as[T].?) { values =>
        inner.apply(Seq.wrap(values.toVector)).toScala
      }
    )
  }  
}
