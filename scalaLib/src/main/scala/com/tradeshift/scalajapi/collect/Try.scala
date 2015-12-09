package com.tradeshift.scalajapi.collect

import scala.util.{Try => STry}

object Try {
  def wrap[T](scalaTry: STry[T]): Try[T] = Try(scalaTry)
  
  def call[T](sup: java.util.function.Supplier[T]): Try[T] = wrap(STry { sup.get() })
  
  def failed(error: Throwable) = wrap(scala.util.Failure(error))
  
  def success[T](value: T) = wrap(scala.util.Success(value))
}

case class Try[T] private (val unwrap: STry[T]) {
  import Try._
  
  def isFailure: Boolean = unwrap.isFailure
  
  def isSuccess: Boolean = unwrap.isSuccess
  
  def getOrElse(f: java.util.function.Supplier[_ <: T]): T = unwrap.getOrElse(f.get)
  
  def orElse(f: java.util.function.Supplier[Try[_ <: T]]): Try[T] = wrap(unwrap.orElse(f.get.unwrap))
  
  def get: T = unwrap.get
  
  def foreach(f: java.util.function.Consumer[T]): Unit = unwrap.foreach(f.accept)
  
  def flatMap[U](f: java.util.function.Function[T,Try[U]]): Try[U] = wrap(unwrap.flatMap(v => f.apply(v).unwrap))
  
  def map[U](f: java.util.function.Function[T,U]): Try[U] = wrap(unwrap.map(f.apply))
  
  def filter(p: java.util.function.Predicate[_ >: T]): Try[T] = wrap(unwrap.filter(p.test)) 
  
  def recover(pf: PartialFunction[Throwable,_ <: T]): Try[T] = wrap(unwrap.recover(pf))
  
  def recoverWith(pf: PartialFunction[Throwable,Try[_ <: T]]): Try[T] = wrap(unwrap.recoverWith(pf.andThen(_.unwrap)))
  
  def toOption: Option[T] = Option.wrap(unwrap.toOption)
  
  def failure: Try[Throwable] = wrap(unwrap.failed)
  
  def transform[U](successFunc: java.util.function.Function[T,Try[U]], 
                   failureFunc: java.util.function.Function[Throwable,Try[U]]) = 
     wrap(unwrap.transform(s => successFunc(s).unwrap, f => failureFunc(f).unwrap))
     
  override def toString: String = unwrap.toString
}
