package com.tradeshift.scalajapi.akka.route

import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling
import scala.concurrent.ExecutionContext
import Unmarshaller.wrap
import com.tradeshift.scalajapi.concurrent.Future

object StringUnmarshaller {
  def STRING = sync[String](java.util.function.Function.identity())
  
  def BYTE = wrap(byteFromStringUnmarshaller)
  def SHORT = wrap(shortFromStringUnmarshaller)
  def INT = wrap(intFromStringUnmarshaller)
  def LONG = wrap(longFromStringUnmarshaller)
  
  def BYTE_HEX = wrap(HexByte)
  def SHORT_HEX = wrap(HexShort)
  def INT_HEX = wrap(HexInt)
  def LONG_HEX = wrap(HexLong)
  
  def FLOAT = wrap(floatFromStringUnmarshaller)
  def DOUBLE = wrap(doubleFromStringUnmarshaller)
  
  def BOOLEAN = wrap(booleanFromStringUnmarshaller)
  
  def UUID = wrap(unmarshalling.Unmarshaller.strict { s: String => java.util.UUID.fromString(s) })
  
  def async[B](f: java.util.function.BiFunction[String,ExecutionContext,Future[B]]): Unmarshaller[String,B] = Unmarshaller.async(f)
  
  def sync[B](f: java.util.function.Function[String,B]): Unmarshaller[String,B] = Unmarshaller.sync(f)
}
