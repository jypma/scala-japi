package com.tradeshift.scalajapi.concurrent

import scala.concurrent
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object Future {
  def wrap[T](scalaFuture: concurrent.Future[T]): Future[T] = new Future(scalaFuture)
}

class Future[T] private (val unwrap: concurrent.Future[T]) {
  import Future._
  private implicit val ctx = ExecutionContext.global

  def mapTo[T1](t: Class[T1]): Future[T1] = wrap(unwrap.mapTo[T1](ClassTag(t)))
  
  def map[T1](f: java.util.function.Function[T,T1]): Future[T1] = wrap(unwrap.map(f(_)))
  
  def flatMap[T1](f: java.util.function.Function[T,Future[T1]]): Future[T1] = wrap(unwrap.flatMap(f(_).unwrap))
}
