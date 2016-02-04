package com.tradeshift.scalajapi.akka.route

import com.tradeshift.scalajapi.collect.Option
import com.tradeshift.scalajapi.collect.Set
import com.tradeshift.scalajapi.collect.Seq
import akka.http.scaladsl.server._
import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.MediaType
import akka.http.scaladsl
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.javadsl.model.headers.HttpEncoding
import akka.http.javadsl.model.headers.ByteRange
import akka.http.javadsl.model.ContentType
import akka.http.javadsl.model.headers.HttpChallenge

object Rejections {
  def method(supported: HttpMethod) = MethodRejection(supported)
  
  def scheme(supported: String) = SchemeRejection(supported)
  
  def missingQueryParam(parameterName: String) = MissingQueryParamRejection(parameterName)
  
  def malformedQueryParam(parameterName: String, errorMsg: String) = 
    MalformedQueryParamRejection(parameterName, errorMsg)
  def malformedQueryParam(parameterName: String, errorMsg: String, cause: Option[Throwable]) = 
    MalformedQueryParamRejection(parameterName, errorMsg, cause.unwrap)
    
  def missingFormField(fieldName: String) = MissingFormFieldRejection(fieldName)
  
  def malformedFormField(fieldName: String, errorMsg: String) = 
    MalformedFormFieldRejection(fieldName, errorMsg)
  def malformedFormField(fieldName: String, errorMsg: String, cause: Option[Throwable]) = 
    MalformedFormFieldRejection(fieldName, errorMsg, cause.unwrap)
  
  def missingHeader(headerName: String) = MissingHeaderRejection(headerName)
  
  def malformedHeader(headerName: String, errorMsg: String) = 
    MalformedHeaderRejection(headerName, errorMsg)
  def malformedHeader(headerName: String, errorMsg: String, cause: Option[Throwable]) = 
    MalformedHeaderRejection(headerName, errorMsg, cause.unwrap)
  
  def unsupportedRequestContentType(supported: Set[MediaType]) = 
    UnsupportedRequestContentTypeRejection(supported.unwrap.map(ContentTypeRange(_)))
    
  def unsupportedRequestEncoding(supported: HttpEncoding) = UnsupportedRequestEncodingRejection(supported)
    
  def unsatisfiableRange(unsatisfiableRanges: Seq[ByteRange], actualEntityLength: Long) =
    UnsatisfiableRangeRejection(unsatisfiableRanges.unwrap, actualEntityLength)
    
  def tooManyRanges(maxRanges: Int) = TooManyRangesRejection(maxRanges)
  
  def malformedRequestContent(message: String) = 
    MalformedRequestContentRejection(message)
  def malformedRequestContent(message: String, cause: Option[Throwable]) = 
    MalformedRequestContentRejection(message, cause.unwrap)
    
  def requestEntityExpected = RequestEntityExpectedRejection
  
  def unacceptedResponseContentType(supportedContentTypes: Set[ContentType], supportedMediaTypes: Set[MediaType]) = 
    UnacceptedResponseContentTypeRejection(
      supportedContentTypes.unwrap.map(t ⇒ (t: scaladsl.model.ContentType): ContentNegotiator.Alternative).toSet ++
      supportedMediaTypes.unwrap.map(t ⇒ (t: scaladsl.model.MediaType): ContentNegotiator.Alternative).toSet)

  
  def unacceptedResponseEncoding(supported: HttpEncoding) =
    UnacceptedResponseEncodingRejection(supported)
  def unacceptedResponseEncoding(supported: Set[HttpEncoding]) = 
    UnacceptedResponseEncodingRejection(supported.unwrap)
    
  def authenticationCredentialsMissing(challenge: HttpChallenge) = 
    AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, challenge)
  def authenticationCredentialsRejected(challenge: HttpChallenge) = 
    AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, challenge)

  def authorizationFailed = AuthorizationFailedRejection
  
  def missingCookie(cookieName: String) = MissingCookieRejection(cookieName)
  
  def expectedWebSocketRequest = ExpectedWebSocketRequestRejection
  
  def validationRejection(message: String) = ValidationRejection(message)
  def validationRejection(message: String, cause: Option[Throwable]) = ValidationRejection(message, cause.unwrap)
  
  def transformationRejection(f: java.util.function.Function[Seq[Rejection], Seq[Rejection]]) =
    TransformationRejection(rejections => f.apply(Seq.wrap(rejections)).unwrap)
  
  def rejectionError(rejection: Rejection) = RejectionError(rejection)
}
