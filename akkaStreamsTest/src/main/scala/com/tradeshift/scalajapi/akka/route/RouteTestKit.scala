package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.collect.Seq
import com.tradeshift.scalajapi.collect.Option
import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.testkit.TestFrameworkInterface
import akka.http.javadsl.model.HttpRequest
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.http.javadsl.model.HttpEntity
import akka.http.javadsl.model.HttpHeader
import scala.reflect.ClassTag
import akka.http.scaladsl.server.Rejection
import akka.http.javadsl.model.ContentType
import akka.http.javadsl.model.MediaType
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.HttpEntity.ChunkStreamPart
import akka.http.javadsl.model.ResponseEntity
import akka.http.javadsl.model.HttpCharset
import akka.http.javadsl.model.StatusCode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class RouteTestKit {
  outer =>
  
  private trait Runner {
    def run(request: HttpRequest, route: Route, checks: => Unit)
  }
  
  /**
   * Literal configuration that will be applied to the actor system that is instantiated
   * for this test class. Override this if you want to add a few settings to an otherwise-
   * valid application.conf or reference.conf.
   * 
   * The default implementation of testConfig() will call this method
   * and combine the result with ConfigFactory.load() as fallback.
   * 
   * This method must not refer to any instance fields, since it is invoked by 
   * RouteTestKit's constructor.  
   */
  def testConfigSource = "" 
  
  /**
   * Returns the Config to be used for the actor system that is instantiated
   * for this test class.
   * 
   * Override this method if you want to have a completely custom Config for the 
   * route under test.
   * 
   * The default implementation will call this testConfigSource() 
   * and combine the result with ConfigFactory.load() as fallback.
   * 
   * This method must not refer to any instance fields, since it is invoked by 
   * RouteTestKit's constructor.  
   */
  def testConfig: Config = {
    val source = testConfigSource
    val config = if (source.isEmpty) ConfigFactory.empty() else ConfigFactory.parseString(source)
    config.withFallback(ConfigFactory.load())
  }
  
  private val kit = new RouteTest with TestFrameworkInterface with Runner {
    override def failTest(msg:String): Nothing = throw new AssertionError(msg)
    
    override def testConfig = outer.testConfig
    
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
  def rejection:Rejection = kit.rejection
  
  def response:HttpResponse = kit.response
  def responseEntity:HttpEntity = kit.responseEntity
  def chunks:Seq[ChunkStreamPart] = Seq.wrap(kit.chunks)
  def contentType:ContentType = kit.contentType
  def mediaType:MediaType = kit.mediaType
  def charset:HttpCharset = kit.charset
  def responseEntityStrict:HttpEntity.Strict = Await.result(kit.response.entity.toStrict(3.seconds), 3.seconds)
  def headers:Seq[HttpHeader] = Seq.wrap(kit.headers)
  def header[T <: akka.http.scaladsl.model.HttpHeader](c:Class[T]):Option[T] = Option.wrap(kit.header[T](ClassTag(c)))
  def status:StatusCode = kit.status
  
  // --- extra non-wrapped methods to make Java API just a little nicer ---
  /** 
   * Returns a single expected rejection, verifying that is indeed an instance of T.  
   */
  def rejection[T <: Rejection](t: Class[T]): T = {
    val r = rejection
    if (t.isInstance(r)) r.asInstanceOf[T] else kit.failTest("Expected a rejection of type %s but got %s".format(t.getSimpleName, r))
  }
  /**
   * Returns whether the request was rejected by the route
   */
  def isRejected:Boolean = { kit.rejections; true }
  /**
   * Returns whether the request was rejected by the route as an empty rejection list
   * (see http://doc.akka.io/docs/akka-stream-and-http-experimental/snapshot/scala/http/routing-dsl/rejections.html#Empty_Rejections
   */
  def isEmptyRejected: Boolean = rejections.isEmpty
}
