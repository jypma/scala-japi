package com.tradeshift.scalajapi.akka.route

import java.util.UUID
import akka.http.scaladsl.server.{Directives => D}
import akka.http.scaladsl.server.PathMatchers
import java.util.function.Supplier
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.server.ValidationRejection

trait PathDirectives {
  def path(element: String, inner: Supplier[Route]): Route = ScalaRoute(
    D.pathPrefix(element) {
      inner.get.toScala
    }
  )

  def path(inner: java.util.function.Function[String,Route]): Route = ScalaRoute(
    D.pathPrefix(PathMatchers.Segment) { element => 
      inner.apply(element).toScala
    }
  )
  
  def path[T](t: Unmarshaller[String, T], inner: java.util.function.Function[T, Route]): Route = ScalaRoute {
    D.pathPrefix(PathMatchers.Segment) { element ⇒
      D.extractRequestContext { ctx ⇒
        import ctx.executionContext
        import ctx.materializer
        D.onComplete(t.unwrap.apply(element)) {
          case Success(value) ⇒
            inner.apply(value).toScala
          case Failure(x: IllegalArgumentException) ⇒
            D.reject(ValidationRejection("not a valid path at this location (" + element + "): " + x.getMessage, Some(x)))
          case Failure(x) ⇒
            D.failWith(x)
        }
      }
    }
  }
}
