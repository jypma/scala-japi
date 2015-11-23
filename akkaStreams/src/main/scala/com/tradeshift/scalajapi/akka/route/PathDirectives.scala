package com.tradeshift.scalajapi.akka.route

import java.util.UUID
import akka.http.scaladsl.server.directives.{PathDirectives => PD}
import akka.http.scaladsl.server.PathMatchers
import java.util.function.Supplier
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.server.ValidationRejection

trait PathDirectives {
  def path(element: String, inner: Supplier[Route]): Route = ScalaRoute(
    PD.pathPrefix(element) {
      inner.get.toScala
    }
  )

  def path(inner: java.util.function.Function[String,Route]): Route = ScalaRoute(
    PD.pathPrefix(PathMatchers.Segment) { element => 
      inner.apply(element).toScala
    }
  )
  
  def path[T](t: Unmarshaller[String,T], inner: java.util.function.Function[T,Route]): Route = {
    import akka.http.scaladsl.server.Directives._
    import scala.concurrent.ExecutionContext.Implicits.global
    
    ScalaRoute(
      PD.pathPrefix(PathMatchers.Segment) { element =>
        onComplete(t.unwrap.apply(element)) {
          case Success(value) =>
            inner.apply(value).toScala
          case Failure(x:IllegalArgumentException) =>
            reject(ValidationRejection("not a valid path at this location (" + element + "): " + x.getMessage, Some(x)))
          case Failure(x) =>
            failWith(x)
        }
      }
    )
  }
}

