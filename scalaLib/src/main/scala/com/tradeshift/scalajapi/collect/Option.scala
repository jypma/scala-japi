package com.tradeshift.scalajapi.collect

import java.util.stream.StreamSupport
import java.util.Spliterators
import java.util.Spliterator

object Option {
  def wrap[T](scalaOption: scala.Option[T]): Option[T] = new Option(scalaOption)
  
  def empty[T]: Option[T] = wrap(scala.Option.empty[T])
  
  def of[T](possiblyNullValue: T): Option[T] = wrap(scala.Option(possiblyNullValue)) 
  
  def of[T](javaOption: java.util.Optional[T]): Option[T] = javaOption match {
    case o if o.isPresent() => wrap(scala.Option(o.get))
    case _ => empty
  }
  
  /**
   * Turns any first element of the given iterable into an Option, throwing
   * IllegalArgumentException if the iterable turns out to have more than 
   * one result.
   */
  def of[T](iterable: java.lang.Iterable[T]): Option[T] = {
    val i = iterable.iterator()
    if (i.hasNext()) {
      val value = Option.of(i.next())
      if (i.hasNext()) {
        throw new IllegalArgumentException("Iterable should have 0 or 1 value, but had more: " + iterable)
      } else {
        value
      }
    } else {
      empty
    }
  }
  
  /**
   * Turns the first element of the given iterable into an Option, or returns
   * Option.empty if the iterable has no elements.
   */
  def ofHead[T](iterable: java.lang.Iterable[T]) = {
    val i = iterable.iterator()
    if (i.hasNext()) {
      Option.of(i.next())
    } else {
      empty
    }    
  }
}

class Option[T] private (val unwrap: scala.Option[T]) extends java.lang.Iterable[T] {
  import Option._
  
  def get:T = unwrap.get
  
  def getOrElse[T1 >: T](f: java.util.function.Supplier[T1]): T1 = unwrap.getOrElse(f.get())
  
  def orElse[T1 >: T](f: java.util.function.Supplier[Option[T1]]): Option[T1] = wrap(unwrap.orElse(f.get().unwrap))
  
  def orNull = getOrElse(null)
  
  def filter(p: java.util.function.Predicate[T]): Option[T] = wrap(unwrap.filter(p.test))
  
  def size:Int = unwrap.size
  
  def isEmpty:Boolean = unwrap.isEmpty

  def isDefined:Boolean = unwrap.isDefined
  
  def map[T1](f: java.util.function.Function[T,T1]): Option[T1] = wrap(unwrap.map(f(_)))
  
  def flatMap[T1](f: java.util.function.Function[T,Option[T1]]): Option[T1] = wrap(unwrap.flatMap(f(_).unwrap))
  
  def mapToMap[K,V](f: java.util.function.Function[T,(K,V)]): Map[K,V] = Map.wrap(unwrap.map(f(_)).toMap)
  
  def toSet: Set[T] = Set.wrap(unwrap.toSet)
  
  def toSeq: Seq[T] = Seq.wrap(unwrap.toVector)
  
  def asStream: java.util.stream.Stream[T] = StreamSupport.stream(
          Spliterators.spliterator(iterator, size, Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE),
          false);
  
  override def iterator = new java.util.Iterator[T] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }  
}
