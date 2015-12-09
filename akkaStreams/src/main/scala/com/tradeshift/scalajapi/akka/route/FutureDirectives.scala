package com.tradeshift.scalajapi.akka.route

import java.util.function.Supplier
import java.util.function.{Function => JFunction}
import com.tradeshift.scalajapi.concurrent.Future
import akka.http.scaladsl.server.directives.{FutureDirectives => D}
import com.tradeshift.scalajapi.collect.Try


trait FutureDirectives {
  def onComplete[T](f: Supplier[Future[T]], inner: JFunction[Try[T], Route]) = ScalaRoute (
    D.onComplete(f.get.unwrap) { value => 
      inner.apply(Try.wrap(value)).toScala 
    }
  )
  
  def onSuccess[T](f: Supplier[Future[T]], inner: JFunction[T, Route]) = ScalaRoute (
    D.onSuccess(f.get.unwrap) { value => 
      inner.apply(value).toScala 
    }
  )
  
}
