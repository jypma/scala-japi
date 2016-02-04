package com.tradeshift.scalajapi.akka

import akka.http.scaladsl
import akka.http.javadsl.model.HttpCharset
import akka.http.javadsl.model.MediaType
import scala.collection.JavaConverters._

object CustomMediaTypes {
  def customBinary(mainType: String, subType: String, compressible: Boolean, fileExtensions: java.lang.Iterable[String],
                   params: java.util.Map[String, String], allowArbitrarySubtypes: Boolean): MediaType.Binary = {
    scaladsl.model.MediaType.customBinary(mainType, subType, 
      if (compressible) scaladsl.model.MediaType.Compressible else scaladsl.model.MediaType.NotCompressible, 
      fileExtensions.asScala.toList, params.asScala.toMap, allowArbitrarySubtypes)
  }
  
  def customWithFixedCharset(mainType: String, subType: String, charset: HttpCharset, fileExtensions: java.lang.Iterable[String],
                             params: java.util.Map[String, String],
                             allowArbitrarySubtypes: Boolean): MediaType.WithFixedCharset = {
    scaladsl.model.MediaType.customWithFixedCharset(mainType, subType, charset.asInstanceOf[scaladsl.model.HttpCharset], 
      fileExtensions.asScala.toList, params.asScala.toMap, allowArbitrarySubtypes)
  }
  
  def customWithOpenCharset(mainType: String, subType: String, fileExtensions: java.lang.Iterable[String],
                            params: java.util.Map[String, String],
                            allowArbitrarySubtypes: Boolean): MediaType.WithOpenCharset = {
    scaladsl.model.MediaType.customWithOpenCharset(mainType, subType, 
      fileExtensions.asScala.toList, params.asScala.toMap, allowArbitrarySubtypes)
  }
  
  def customMultipart(subType: String, params: java.util.Map[String, String]): MediaType.Multipart = {
    scaladsl.model.MediaType.customMultipart(subType, params.asScala.toMap)
  }  
}
