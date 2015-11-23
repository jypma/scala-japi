package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.unmarshalling
import scala.concurrent.ExecutionContext
import com.tradeshift.scalajapi.concurrent.Future
import com.tradeshift.scalajapi.collect.Seq
import akka.stream.Materializer
import scala.annotation.varargs
import akka.http.javadsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl
import akka.http.javadsl.model.ContentType
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.RequestEntity
import akka.http.javadsl.model.MediaType

object Unmarshaller {
  private implicit val _ = javaToScalaHttpEntity
  
  def wrap[A,B](scalaUnmarshaller: unmarshalling.Unmarshaller[A,B]) = Unmarshaller()(scalaUnmarshaller)

  /**
   * Creates an unmarshaller from an asynchronous Java function. The function should use the given execution
   * context for its returned futures. 
   */
  def async[A,B](f: java.util.function.BiFunction[A,ExecutionContext,Future[B]]) = wrap(unmarshalling.Unmarshaller[A,B] {
    ctx => a => f.apply(a, ctx).unwrap
  })
  
  /**
   * Creates an unmarshaller from a Java function.
   */
  def sync[A,B](f: java.util.function.Function[A,B]) = wrap(unmarshalling.Unmarshaller[A,B] {
    ctx => a => scala.concurrent.Future.successful(f.apply(a))
  })
  
  def entityToByteString(implicit materializer: Materializer) = 
    wrapFromHttpEntity(unmarshalling.Unmarshaller.byteStringUnmarshaller)
  
  def entityToByteArray(implicit materializer: Materializer) = 
    wrapFromHttpEntity(unmarshalling.Unmarshaller.byteArrayUnmarshaller)
  
  def entityToCharArray(implicit materializer: Materializer) = 
    wrapFromHttpEntity(unmarshalling.Unmarshaller.charArrayUnmarshaller)
  
  def entityToString(implicit materializer: Materializer) = 
    wrapFromHttpEntity(unmarshalling.Unmarshaller.stringUnmarshaller)
  
  def entityToUrlEncodedFormData(implicit materializer: Materializer) = 
    wrapFromHttpEntity(unmarshalling.Unmarshaller.defaultUrlEncodedFormDataUnmarshaller)
  
  def requestToEntity = wrap(unmarshalling.Unmarshaller[HttpRequest,RequestEntity] {
    ctx => request => scala.concurrent.Future.successful(request.entity())
  })
  
  def forMediaType[B](t: MediaType, um:Unmarshaller[_ <: HttpEntity,B]): Unmarshaller[HttpEntity,B] = {
    implicit val _ = javaToScalaHttpEntity
    
    wrap(toScala(um.unwrap).forContentTypes(t: scaladsl.model.MediaType))
  }
  
  def forMediaTypes[B](types: Seq[MediaType], um:Unmarshaller[_ <: HttpEntity,B]): Unmarshaller[HttpEntity,B] = {
    implicit val _ = javaToScalaHttpEntity

    wrap(toScala(um.unwrap).forContentTypes(types.unwrap.map(ContentTypeRange(_)): _*))
  }
  
  def firstOf[A, B] (u1: Unmarshaller[A, B], u2: Unmarshaller[A, B]): Unmarshaller[A, B] = {
    wrap(unmarshalling.Unmarshaller.firstOf(u1.unwrap, u2.unwrap))
  }
  
  def firstOf[A, B] (u1: Unmarshaller[A, B], u2: Unmarshaller[A, B], u3: Unmarshaller[A, B]): Unmarshaller[A, B] = {
    wrap(unmarshalling.Unmarshaller.firstOf(u1.unwrap, u2.unwrap, u3.unwrap))
  }
  
  def firstOf[A, B] (u1: Unmarshaller[A, B], u2: Unmarshaller[A, B], u3: Unmarshaller[A, B], u4: Unmarshaller[A, B]): Unmarshaller[A, B] = {
    wrap(unmarshalling.Unmarshaller.firstOf(u1.unwrap, u2.unwrap, u3.unwrap, u4.unwrap))
  }
  
  def firstOf[A, B] (u1: Unmarshaller[A, B], u2: Unmarshaller[A, B], u3: Unmarshaller[A, B], u4: Unmarshaller[A, B], u5: Unmarshaller[A, B]): Unmarshaller[A, B] = {
    wrap(unmarshalling.Unmarshaller.firstOf(u1.unwrap, u2.unwrap, u3.unwrap, u4.unwrap, u5.unwrap))
  }
  
  private def wrapFromHttpEntity[B](scalaUnmarshaller: unmarshalling.Unmarshaller[scaladsl.model.HttpEntity,B]) = {
    wrap(scalaUnmarshaller: unmarshalling.Unmarshaller[HttpEntity,B])
  }
    
}

case class Unmarshaller[A,B] private (implicit val unwrap: unmarshalling.Unmarshaller[A,B]) {
  import unmarshalling.Unmarshaller._
  import Unmarshaller.wrap
    
  def map[C](f: java.util.function.Function[B,C]): Unmarshaller[A, C] = wrap(unwrap.map(f.apply))
  
  def flatMap[C](f: java.util.function.BiFunction[ExecutionContext,B,Future[C]]): Unmarshaller[A, C] = 
    wrap(unwrap.flatMap { ctx => a => f.apply(ctx, a).unwrap })
  
  def flatMap[C](u: Unmarshaller[_ >: B,C]): Unmarshaller[A,C] =
    wrap(unwrap.flatMap { ctx => a => u.unwrap.apply(a)(ctx) })
    
  def mapWithInput[C](f: java.util.function.BiFunction[A, B, C]): Unmarshaller[A, C] =
    wrap(unwrap.mapWithInput { case (a,b) => f.apply(a, b) })

  def flatMapWithInput[C](f: java.util.function.BiFunction[A, B, Future[C]]): Unmarshaller[A, C] =
    wrap(unwrap.flatMapWithInput { case (a,b) => f.apply(a, b).unwrap })
}
