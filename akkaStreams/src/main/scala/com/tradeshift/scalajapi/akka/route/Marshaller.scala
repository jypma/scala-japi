package com.tradeshift.scalajapi.akka.route

import java.util.function
import akka.http.scaladsl.marshalling
import scala.concurrent.ExecutionContext
import akka.http.javadsl.model.ContentType
import akka.http.scaladsl
import akka.http.javadsl.model.HttpEntity
import akka.http.scaladsl.marshalling.MediaTypeOverrider
import akka.http.scaladsl.marshalling._
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.RequestEntity
import akka.util.ByteString
import akka.http.scaladsl.model.FormData

object Marshaller {
  def wrap[A,B](scalaMarshaller: marshalling.Marshaller[A,B]) = Marshaller()(scalaMarshaller)
  
  /** 
   * Safe downcasting of the output type of the marshaller to a superclass. 
   *  
   * Marshaller is covariant in B, i.e. if B2 is a subclass of B1, 
   * then Marshaller[X,B2] is OK to use where Marshaller[X,B1] is expected. 
   */
  def downcast[A, B1, B2 <: B1](m: Marshaller[A,B2]): Marshaller[A,B1] = m.asInstanceOf[Marshaller[A,B1]]

  /** 
   * Safe downcasting of the output type of the marshaller to a superclass. 
   *  
   * Marshaller is covariant in B, i.e. if B2 is a subclass of B1, 
   * then Marshaller[X,B2] is OK to use where Marshaller[X,B1] is expected. 
   */
  def downcast[A, B1, B2 <: B1](m: Marshaller[A,B2], target: Class[B1]): Marshaller[A,B1] = m.asInstanceOf[Marshaller[A,B1]]
  
  def stringToEntity: Marshaller[String,RequestEntity] = wrap(marshalling.Marshaller.StringMarshaller)

  def byteArrayToEntity: Marshaller[Array[Byte],RequestEntity] = wrap(marshalling.Marshaller.ByteArrayMarshaller)

  def charArrayToEntity: Marshaller[Array[Char],RequestEntity] = wrap(marshalling.Marshaller.CharArrayMarshaller)
  
  def byteStringToEntity: Marshaller[ByteString,RequestEntity] = wrap(marshalling.Marshaller.ByteStringMarshaller)
  
  def fromDataToEntity: Marshaller[FormData,RequestEntity] = wrap(marshalling.Marshaller.FormDataMarshaller)
  
  def wrapEntity[A,C](m: Marshaller[A,_ <: RequestEntity], contentType: ContentType, f:function.BiFunction[ExecutionContext,C,A]): Marshaller[C,RequestEntity] = {
    val scalaMarshaller = toScala(downcast(m, classOf[RequestEntity]).unwrap)
    wrap(scalaMarshaller.wrapWithEC(contentType) { ctx => c:C => f(ctx,c) } ) 
  }

  def wrapEntity[A,C](m: Marshaller[A,_ <: RequestEntity], contentType: ContentType, f:function.Function[C,A]): Marshaller[C,RequestEntity] = {
    val scalaMarshaller = toScala(downcast(m, classOf[RequestEntity]).unwrap)
    wrap(scalaMarshaller.wrap(contentType)(f.apply)) 
  }

  def wrapResponse[A,C](m: Marshaller[A,HttpResponse], contentType: ContentType, f:function.BiFunction[ExecutionContext,C,A]): Marshaller[C,HttpResponse] = {
    wrap(toScala(m.unwrap).wrapWithEC(contentType) { ctx => c:C => f(ctx,c) }) 
  }
  
  def wrapResponse[A,C](m: Marshaller[A,HttpResponse], contentType: ContentType, f:function.Function[C,A]): Marshaller[C,HttpResponse] = {
    wrap(toScala(m.unwrap).wrap(contentType)(f.apply)) 
  }
  
  def wrapRequest[A,C](m: Marshaller[A,HttpRequest], contentType: ContentType, f:function.BiFunction[ExecutionContext,C,A]): Marshaller[C,HttpRequest] = {
    wrap(toScala(m.unwrap).wrapWithEC(contentType) { ctx => c:C => f(ctx,c) }) 
  }
  
  def wrapRequest[A,C](m: Marshaller[A,HttpRequest], contentType: ContentType, f:function.Function[C,A]): Marshaller[C,HttpRequest] = {
    wrap(toScala(m.unwrap).wrap(contentType)(f.apply)) 
  }
  
  def entityToResponse[A](m: Marshaller[A,_ <: RequestEntity]): Marshaller[A,HttpResponse] = {
    wrap(marshalling.Marshaller.fromToEntityMarshaller[A]()(m.unwrap))
  }
  
  def oneOf[A, B] (m1: Marshaller[A, B], m2: Marshaller[A, B]): Marshaller[A, B] = {
    wrap(marshalling.Marshaller.oneOf(m1.unwrap, m2.unwrap))
  }
  
  def oneOf[A, B] (m1: Marshaller[A, B], m2: Marshaller[A, B], m3: Marshaller[A, B]): Marshaller[A, B] = {
    wrap(marshalling.Marshaller.oneOf(m1.unwrap, m2.unwrap, m3.unwrap))
  }
  
  def oneOf[A, B] (m1: Marshaller[A, B], m2: Marshaller[A, B], m3: Marshaller[A, B], m4: Marshaller[A, B]): Marshaller[A, B] = {
    wrap(marshalling.Marshaller.oneOf(m1.unwrap, m2.unwrap, m3.unwrap, m4.unwrap))
  }

  def oneOf[A, B] (m1: Marshaller[A, B], m2: Marshaller[A, B], m3: Marshaller[A, B], m4: Marshaller[A, B], m5: Marshaller[A, B]): Marshaller[A, B] = {
    wrap(marshalling.Marshaller.oneOf(m1.unwrap, m2.unwrap, m3.unwrap, m4.unwrap, m5.unwrap))
  }
}

case class Marshaller[A,B] private (implicit val unwrap: marshalling.Marshaller[A,B]) {
  
}
