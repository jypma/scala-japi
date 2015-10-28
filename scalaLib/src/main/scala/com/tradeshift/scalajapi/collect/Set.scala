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
import java.util.stream.StreamSupport
import java.util.Spliterators
import java.util.Spliterator

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
    override def characteristics = Set.of(Collector.Characteristics.UNORDERED).asSet 
  }
  
  def widen[U, T >: U](set: Set[T]): Set[U] = set.asInstanceOf[Set[U]]  
}

case class Set[T] private (val unwrap: immutable.Set[T]) extends java.lang.Iterable[T] {
  import Set._
  
  def filter(p: java.util.function.Predicate[T]): Set[T] = wrap(unwrap.filter(p.test))
  
  def size:Int = unwrap.size

  def isEmpty:Boolean = unwrap.isEmpty
  
  def plus(item: T): Set[T] = wrap(unwrap + item)
  
  def plusAll(items: java.lang.Iterable[T]) = wrap(unwrap ++ items.asScala)
  
  def minus(item: T): Set[T] = wrap(unwrap - item)
  
  def minusAll(items: java.lang.Iterable[T]) = wrap(unwrap -- items.asScala)
  
  def map[T1](f: java.util.function.Function[T,T1]): Set[T1] = wrap(unwrap.map(f(_)))
  
  def flatMap[T1](f: java.util.function.Function[T,java.lang.Iterable[T1]]): Set[T1] = wrap(unwrap.flatMap(f(_).asScala))

  def mapToMap[K,V](f: java.util.function.Function[T,(K,V)]): Map[K,V] = Map.wrap(unwrap.map(f(_)).toMap)
  
  def toSeq: Seq[T] = Seq.wrap(unwrap.toVector)
  
  def asSet: java.util.Set[T] = unwrap.asJava
  
  def toHashSet: java.util.HashSet[T] = new java.util.HashSet(asSet)
  
  def asStream: java.util.stream.Stream[T] = StreamSupport.stream(
          Spliterators.spliterator(iterator, size, Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE),
          false);
  
  override def iterator = new java.util.Iterator[T] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }
}
