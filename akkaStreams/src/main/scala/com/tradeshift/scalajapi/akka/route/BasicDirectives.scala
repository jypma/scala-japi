package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.server.directives.{BasicDirectives => D}

trait BasicDirectives {
  def extract[T](f: java.util.function.Function[RequestContext,T], inner: java.util.function.Function[T,Route]): Route = {
    val extractF = D.extract { ctx => f.apply(RequestContext(ctx)) }
    ScalaRoute ( extractF { value => inner.apply(value).toScala } )
  }
}
