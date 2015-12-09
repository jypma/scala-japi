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

object SortedSet {
  def wrap[T](scalaSet: immutable.SortedSet[T]): SortedSet[T] = new SortedSet(scalaSet)
  
  def empty[T <: Comparable[T]]: SortedSet[T] = wrap(immutable.SortedSet.empty)
  
  def of[T <: Comparable[T]](t1: T): SortedSet[T] = wrap(immutable.SortedSet(t1))
  def of[T <: Comparable[T]](t1: T, t2:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T, t5:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4,t5))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T, t5:T, t6:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4,t5,t6))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T, t5:T, t6:T, t7:T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4,t5,t6,t7))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T, t5:T, t6:T, t7:T, t8: T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4,t5,t6,t7,t8))
  def of[T <: Comparable[T]](t1: T, t2:T, t3:T, t4:T, t5:T, t6:T, t7:T, t8: T, t9: T): SortedSet[T] = wrap(immutable.SortedSet(t1,t2,t3,t4,t5,t6,t7,t8,t9))
  
  
  def ofAll[T <: Comparable[T]](items: java.lang.Iterable[T]): SortedSet[T] = wrap(immutable.SortedSet.empty[T] ++ items.asScala)
  
  def collector[T <: Comparable[T]]:Collector[T,mutable.SortedSet[T],SortedSet[T]] = new Collector[T,mutable.SortedSet[T],SortedSet[T]] {
    override def supplier = new Supplier[mutable.SortedSet[T]] { 
      def get = mutable.SortedSet.empty[T] }
    override def accumulator = new BiConsumer[mutable.SortedSet[T],T] { 
      def accept(s:mutable.SortedSet[T],t:T) = s.add(t) }
    override def combiner = new BinaryOperator[mutable.SortedSet[T]] { 
      def apply(a:mutable.SortedSet[T], b:mutable.SortedSet[T]) = a ++ b }
    override def finisher = new java.util.function.Function[mutable.SortedSet[T],SortedSet[T]] { 
      def apply(s:mutable.SortedSet[T]) = wrap(immutable.SortedSet.empty[T] ++ s) }
    override def characteristics = Set.empty.asSet 
  }
  
  def widen[U, T >: U](SortedSet: SortedSet[T]): SortedSet[U] = SortedSet.asInstanceOf[SortedSet[U]]  
}

case class SortedSet[T] private (val unwrap: immutable.SortedSet[T]) extends java.lang.Iterable[T] {
  import SortedSet._
  
  def contains(item: T): Boolean = unwrap.contains(item)
  
  def filter(p: java.util.function.Predicate[T]): SortedSet[T] = wrap(unwrap.filter(p.test))
  
  def size:Int = unwrap.size

  def isEmpty:Boolean = unwrap.isEmpty
  
  def plus(item: T): SortedSet[T] = wrap(unwrap + item)
  
  def plusAll(items: java.lang.Iterable[T]) = wrap(unwrap ++ items.asScala)
  
  def minus(item: T): SortedSet[T] = wrap(unwrap - item)
  
  def minusAll(items: java.lang.Iterable[T]) = wrap(unwrap -- items.asScala)
  
  def map[T1 <: Comparable[T1]](f: java.util.function.Function[T,T1]): SortedSet[T1] = wrap(unwrap.map(f(_)))
    
  def flatMap[T1 <: Comparable[T1]](f: java.util.function.Function[T,java.lang.Iterable[T1]]): SortedSet[T1] = wrap(unwrap.flatMap(f(_).asScala))

  def mapToSet[T1](f: java.util.function.Function[T,T1]): Set[T1] = Set.wrap(unwrap.map(f(_)))
  
  def mapToMap[K,V](f: java.util.function.Function[T,(K,V)]): Map[K,V] = Map.wrap(unwrap.map(f(_)).toMap)
  
  def toSeq: Seq[T] = Seq.wrap(unwrap.toVector)
  
  def asSet: java.util.Set[T] = unwrap.asJava
  
  def toTreeSet: java.util.TreeSet[T] = new java.util.TreeSet(unwrap.asJava)
  
  def asStream: java.util.stream.Stream[T] = StreamSupport.stream(
          Spliterators.spliterator(iterator, size, Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE),
          false);
  
  def foreach(f: java.util.function.Consumer[T]) = unwrap.foreach(f.accept)
  
  def exists(p: java.util.function.Predicate[T]): Boolean = unwrap.exists(p.test)
  
  def forall(p: java.util.function.Predicate[T]): Boolean = unwrap.forall(p.test)
  
  override def iterator: java.util.Iterator[T] = new java.util.Iterator[T] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }

  override def toString: String = unwrap.toString
}
