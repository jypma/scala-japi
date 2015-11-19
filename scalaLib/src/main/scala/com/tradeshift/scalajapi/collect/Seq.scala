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

object Seq {
  def wrap[T](scalaSeq: immutable.Seq[T]): Seq[T] = new Seq(scalaSeq)
  
  def empty[T]: Seq[T] = wrap(immutable.Seq.empty)
  
  def of[T](t1: T): Seq[T] = wrap(immutable.Seq(t1))
  
  def ofAll[T](items: java.lang.Iterable[T]): Seq[T] = wrap(items.asScala.toVector)
  
  private type Builder[T] = mutable.Builder[T,immutable.Seq[T]]
  
  def collector[T]:Collector[T,Builder[T],Seq[T]] = new Collector[T,Builder[T],Seq[T]] {
    private type B = Builder[T]
    override def supplier = new Supplier[B] { def get = immutable.Seq.newBuilder[T] }
    override def accumulator = new BiConsumer[B,T] { def accept(s:B, t:T) = s += t }
    override def combiner = new BinaryOperator[B] { def apply(a:B, b:B) = a ++= b.result() }
    override def finisher = new java.util.function.Function[B,Seq[T]] { def apply(s:B) = wrap(s.result()) }
    override def characteristics = java.util.Collections.emptySet() 
  }

  def widen[U, T >: U](seq: Seq[T]): Seq[U] = seq.asInstanceOf[Seq[U]]
}

// TODO filterWithIndex, mapWithIndex etc.
case class Seq[T] private (val unwrap: immutable.Seq[T]) extends java.lang.Iterable[T] {
  import Seq._

  def filter(p: java.util.function.Predicate[T]): Seq[T] = wrap(unwrap.filter(p.test))
  
  def size:Int = unwrap.size
  
  def isEmpty:Boolean = unwrap.isEmpty

  def head:T = unwrap.head
  
  def headOption:Option[T] = Option.wrap(unwrap.headOption)
  
  def tail:Seq[T] = wrap(unwrap.tail)
  
  def last:T = unwrap.last
  
  def indexOf(elem: T): Int = unwrap.indexOf(elem)
  
  def indexWhere(p: java.util.function.Predicate[T]): Int = unwrap.indexWhere(p.test)
  
  def take(n: Int): Seq[T] = wrap(unwrap.take(n))
  
  def drop(n: Int): Seq[T] = wrap(unwrap.drop(n))
  
  def splitAt(index: Int): (Seq[T], Seq[T]) = {
    val (left, right) = unwrap.splitAt(index)
    (wrap(left), wrap(right))
  }
  
  def get(index: Int):T = unwrap(index)
  
  def plus(item: T): Seq[T] = wrap(unwrap :+ item)
  
  def plusFirst(item: T): Seq[T] = wrap(item +: unwrap)
  
  def plusAll(items: java.lang.Iterable[_ <: T]): Seq[T] = wrap(unwrap ++ items.asScala)
  
  def plusFirstAll(items: java.lang.Iterable[_ <: T]): Seq[T] = wrap(items.asScala ++: unwrap)
  
  def minusIndex(index: Int): Seq[T] = {
    val b = unwrap.companion.newBuilder[T]
    var i = 0
    unwrap.foreach{ x => if (i != index) { b += x }; i += 1 }
    wrap(b.result)
  }
  
  def updated(index: Int, value: T): Seq[T] = wrap(unwrap.updated(index, value))
  
  def inserted(index: Int, value: T): Seq[T] = {
    val builder = unwrap.companion.newBuilder[T]
    builder ++= unwrap.slice(0, index)
    builder += value
    builder ++= unwrap.slice(index, unwrap.size)
    wrap(builder.result())
  }
  
  def map[T1](f: java.util.function.Function[T,T1]): Seq[T1] = wrap(unwrap.map(f(_)))
  
  def flatMap[T1](f: java.util.function.Function[T,java.lang.Iterable[T1]]): Seq[T1] = wrap(unwrap.flatMap(f(_).asScala))
  
  def mapToMap[K,V](f: java.util.function.Function[T,(K,V)]): Map[K,V] = Map.wrap(unwrap.map(f(_)).toMap)
  
  def toSet: Set[T] = Set.wrap(unwrap.toSet)

  def asList: java.util.List[T] = unwrap.asJava
  
  def toArrayList: java.util.ArrayList[T] = new java.util.ArrayList(asList)
  
  def asStream: java.util.stream.Stream[T] = StreamSupport.stream(
          Spliterators.spliterator(iterator, size, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE),
          false);
  
  override def iterator: java.util.Iterator[T] = new java.util.Iterator[T] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }
}
 
