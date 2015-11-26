package com.tradeshift.scalajapi.akka

import akka.http.javadsl
import akka.http.scaladsl
import scala.language.implicitConversions

package object route {
  // We define implicit conversions from the javadsl model objects to the scaladsl subclasses,
  // for all types where there is only 1 valid direct subclass of the javadsl type
  // (being the scaladsl one)
  
  // These are gathered here rather than doing explicit asInstanceOf throughout the code, in order
  // to get predictable compile errors if this hierarchy might ever change.
  
  implicit def toScala[T,J,S](o: scaladsl.marshalling.Marshaller[T,J])(implicit c:TypeEquivalent[J,S]) = 
    o.asInstanceOf[scaladsl.marshalling.Marshaller[T,S]]
  implicit def toJava[J,S,T](o: scaladsl.unmarshalling.Unmarshaller[S,T])(implicit c:TypeEquivalent[J,S]) =
    o.asInstanceOf[scaladsl.unmarshalling.Unmarshaller[J,T]]
  implicit def toScala[J,S,T](o: scaladsl.unmarshalling.Unmarshaller[J,T])(implicit c:TypeEquivalent[J,S]) =
    o.asInstanceOf[scaladsl.unmarshalling.Unmarshaller[S,T]]
  implicit def toScala[J,S](j: J)(implicit c:TypeEquivalent[J,S]): S = 
    j.asInstanceOf[S]
  implicit def toScala[J,S](seq: Seq[J])(implicit c:TypeEquivalent[J,S]): Seq[S] = 
    seq.asInstanceOf[Seq[S]]
  implicit def toScala[J,S](seq: Set[J])(implicit c:TypeEquivalent[J,S]): Set[S] = 
    seq.asInstanceOf[Set[S]]
  implicit def toJava[JI,SI,O,M](flow: akka.stream.scaladsl.Flow[SI,O,M])(implicit c1:TypeEquivalent[JI,SI]) =
    flow.asInstanceOf[akka.stream.scaladsl.Flow[JI,O,M]]
  
  /**
   * Marker class to indicate that either 
   * - [J] has only one direct subclass, being [S]
   * OR
   * - all Java DSL subtypes of [J] have scala DSL subclasses that are also subclasses of [S] 
   */
  case class TypeEquivalent[-J, S]()
  implicit val javaToScalaMediaType = TypeEquivalent[javadsl.model.MediaType, scaladsl.model.MediaType]
  implicit val javaToScalaContentType = TypeEquivalent[javadsl.model.ContentType, scaladsl.model.ContentType]
  implicit val javaToScalaHttpMethod = TypeEquivalent[javadsl.model.HttpMethod, scaladsl.model.HttpMethod]
  implicit val javaToScalaHttpRequest = TypeEquivalent[javadsl.model.HttpRequest, scaladsl.model.HttpRequest]
  implicit val javaToScalaRequestEntity = TypeEquivalent[javadsl.model.RequestEntity, scaladsl.model.RequestEntity]
  implicit val javaToScalaHttpResponse = TypeEquivalent[javadsl.model.HttpResponse, scaladsl.model.HttpResponse]
  implicit val javaToScalaStatusCode = TypeEquivalent[javadsl.model.StatusCode, scaladsl.model.StatusCode]
  implicit val javaToScalaHttpEncoding = TypeEquivalent[javadsl.model.headers.HttpEncoding, scaladsl.model.headers.HttpEncoding]
  implicit val javaToScalaByteRange = TypeEquivalent[javadsl.model.headers.ByteRange, scaladsl.model.headers.ByteRange]
  implicit val javaToScalaHttpChallenge = TypeEquivalent[javadsl.model.headers.HttpChallenge, scaladsl.model.headers.HttpChallenge]
  
  // not made implicit since it's a subtype
  val javaToScalaHttpEntity = TypeEquivalent[javadsl.model.HttpEntity, scaladsl.model.HttpEntity]
}
