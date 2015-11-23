package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.collect.Seq
import com.tradeshift.scalajapi.collect.Option
import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.testkit.TestFrameworkInterface
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpEntityStrict
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.http.javadsl.model.HttpEntity
import akka.http.javadsl.model.ChunkStreamPart
import akka.http.javadsl.model.HttpHeader
import scala.reflect.ClassTag
import akka.http.scaladsl.server.Rejection
import akka.http.javadsl.model.ContentType
import akka.http.javadsl.model.MediaType
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.ResponseEntity
import akka.http.javadsl.model.HttpCharset
import akka.http.javadsl.model.StatusCode

class RouteTestKit {
  private trait Runner {
    def run(request: HttpRequest, route: Route, checks: => Unit)
  }
  
  private val kit = new RouteTest with TestFrameworkInterface with Runner {
    override def failTest(msg:String): Nothing = throw new AssertionError(msg)
    
    def run(request: HttpRequest, route: Route, checks: => Unit) {
      request.asInstanceOf[akka.http.scaladsl.model.HttpRequest] ~> route.toScala ~> check {
        checks
      }
    }
  }
  
  implicit def system = kit.system
  implicit def materializer = kit.materializer
  
  def on(request: HttpRequest, route: Route, checks: java.lang.Runnable) {
    kit.run(request, route, checks.run())
  }
  
  def onSealed(request: HttpRequest, route: Route, checks: java.lang.Runnable) {
    kit.run(request, route.seal(system, materializer), checks.run())
  }
  
  def handled:Boolean = kit.handled
  def rejections:Seq[Rejection] = Seq.wrap(kit.rejections)
  def response:HttpResponse = kit.response
  def responseEntity:HttpEntity = kit.responseEntity
  def chunks:Seq[ChunkStreamPart] = Seq.wrap(kit.chunks)
  def contentType:ContentType = kit.contentType
  def mediaType:MediaType = kit.mediaType
  def charset:HttpCharset = kit.charset
  def responseEntityStrict:HttpEntityStrict = Await.result(kit.response.entity.toStrict(3.seconds), 3.seconds)
  def headers:Seq[HttpHeader] = Seq.wrap(kit.headers)
  def header[T <: akka.http.scaladsl.model.HttpHeader](c:Class[T]):Option[T] = Option.wrap(kit.header[T](ClassTag(c)))
  def status:StatusCode = kit.status
}
