package com.tradeshift.scalajapi.collect

import scala.collection.immutable
import scala.collection.mutable
import scala.annotation.varargs
import scala.collection.JavaConverters._
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.function.Supplier
import java.util.function.BiConsumer
import java.util.function.BinaryOperator

object Set {
  def wrap[T](scalaSet: immutable.Set[T]): Set[T] = new Set(scalaSet)
  
  def empty[T]: Set[T] = wrap(immutable.Set.empty)
  
  def of[T](t1: T): Set[T] = wrap(immutable.Set(t1))
  
  def ofAll[T](items: java.lang.Iterable[T]): Set[T] = wrap(items.asScala.toSet)
  
  def collector[T]:Collector[T,mutable.Set[T],Set[T]] = new Collector[T,mutable.Set[T],Set[T]] {
    override def supplier = new Supplier[mutable.Set[T]] { def get = mutable.Set.empty[T] }
    override def accumulator = new BiConsumer[mutable.Set[T],T] { def accept(s:mutable.Set[T],t:T) = s.add(t) }
    override def combiner = new BinaryOperator[mutable.Set[T]] { def apply(a:mutable.Set[T], b:mutable.Set[T]) = a ++ b }
    override def finisher = new java.util.function.Function[mutable.Set[T],Set[T]] { def apply(s:mutable.Set[T]) = wrap(s.toSet) }
    override def characteristics = java.util.Collections.emptySet() 
  }
}

class Set[T] private (val unwrap: immutable.Set[T]) extends java.lang.Iterable[T] {
  import Set._
  
  def plus(item: T): Set[T] = wrap(unwrap + item)
  
  def map[T1](f: java.util.function.Function[T,T1]): Set[T1] = wrap(unwrap.map(f(_)))
  
  override def iterator = new java.util.Iterator[T] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }
}
