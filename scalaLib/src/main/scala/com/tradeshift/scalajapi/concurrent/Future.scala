package com.tradeshift.scalajapi.concurrent

import scala.concurrent
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.collection.JavaConverters._
import com.tradeshift.scalajapi.collect.{Seq => JSeq, Option => JOption}
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.time.Instant
import java.util.concurrent.TimeoutException

object Future {
  private implicit val ctx = ExecutionContext.global
  
  def wrap[T](scalaFuture: concurrent.Future[T]): Future[T] = new Future(scalaFuture)
  
  def wrap[T](context: ExecutionContext, scalaFuture: concurrent.Future[T]): Future[T] = new Future(scalaFuture, context)
  
  def successful[T](value: T): Future[T] = wrap(concurrent.Future.successful(value))
  
  def failed[T](exception: Throwable): Future[T] = wrap(concurrent.Future.failed(exception))

  def call[T](body: java.util.function.Supplier[T]): Future[T] = wrap(concurrent.Future(body.get))
  
  def call[T](context: ExecutionContext, body: java.util.function.Supplier[T]): Future[T] = 
    wrap(context, concurrent.Future(body.get))
  
  def sequence[T](futures: java.lang.Iterable[Future[T]]): Future[JSeq[T]] = 
    wrap(concurrent.Future.sequence[T,collection.immutable.Seq](futures.asScala.toVector.map(_.unwrap)).map(JSeq.wrap))
    
  def firstCompletedOf[T](futures: java.lang.Iterable[Future[T]]): Future[T] =
    wrap(concurrent.Future.firstCompletedOf(futures.asScala.toSeq.map(_.unwrap)))
  
  def find[T](futures: java.lang.Iterable[Future[T]], p: java.util.function.Predicate[T]): Future[JOption[T]] =
    wrap(concurrent.Future.find(futures.asScala.toSeq.map(_.unwrap))(p.test).map(JOption.wrap))
}

case class Future[T] private (val unwrap: concurrent.Future[T], implicit val context: ExecutionContext = ExecutionContext.global) {
  import Future.wrap

  def on(ctx: ExecutionContext): Future[T] = copy(context = ctx)
  
  def mapTo[T1](t: Class[T1]): Future[T1] = wrap(unwrap.mapTo[T1](ClassTag(t)))
  
  def map[T1](f: java.util.function.Function[T,T1]): Future[T1] = wrap(unwrap.map(f(_)))
  
  def flatMap[T1](f: java.util.function.Function[T,Future[T1]]): Future[T1] = wrap(unwrap.flatMap(f(_).unwrap))
  
  def recover(pf: PartialFunction[Throwable,_ <: T]): Future[T] = wrap(unwrap.recover(pf))
  
  def recoverWith(pf: PartialFunction[Throwable,Future[_ <: T]]): Future[T] = wrap(unwrap.recoverWith(pf.andThen(_.unwrap)))

  def failure: Future[Throwable] = wrap(unwrap.failed)
}
