package com.tradeshift.scalajapi.akka

import akka.http.scaladsl.model.MediaType
import scala.collection.JavaConverters._

object CustomMediaTypes {
  def binary = MediaType.Encoding.Binary
  
  def custom(
    mainType: String, 
    subType: String, 
    encoding: MediaType.Encoding,
    compressible: Boolean, 
    fileExtensions: java.lang.Iterable[String],
    params: Map[String, String], 
    allowArbitrarySubtypes: Boolean): MediaType = 
  MediaType.custom(mainType, subType, encoding, compressible, fileExtensions.asScala.toVector, params, allowArbitrarySubtypes)
  
  def custom(
    mainType: String, 
    subType: String, 
    encoding: MediaType.Encoding,
    params: Map[String, String]): MediaType =
  custom(mainType, subType, encoding, false, Nil.asJava, params, false)
}
