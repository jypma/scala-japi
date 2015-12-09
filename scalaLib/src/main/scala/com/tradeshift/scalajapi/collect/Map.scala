package com.tradeshift.scalajapi.collect

import scala.collection.mutable
import scala.collection.immutable
import scala.collection.JavaConverters._
import java.util.stream.Collector
import java.util.function.Supplier
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.stream.StreamSupport
import java.util.Spliterators
import java.util.Spliterator

object Map {
  def wrap[K,V](scalaMap: immutable.Map[K,V]): Map[K,V] = new Map(scalaMap)
  
  def empty[K,V]: Map[K,V] = wrap(immutable.Map.empty)
  
  def of[K,V](k1: K, v1: V): Map[K,V] = wrap(immutable.Map(k1 -> v1))
  
  def of[K,V](javaMap: java.util.Map[K,V]) = wrap(javaMap.asScala.toMap)
  
  private type Builder[K,V] = mutable.Builder[(K,V),immutable.Map[K,V]]
  
  def collector[K,V]:Collector[(K,V),Builder[K,V],Map[K,V]] = new Collector[(K,V),Builder[K,V],Map[K,V]] {
    private type B = Builder[K,V]
    override def supplier = new Supplier[B] { def get = immutable.Map.newBuilder[K,V] }
    override def accumulator = new BiConsumer[B,(K,V)] { def accept(s:B, t:(K,V)) = s += t }
    override def combiner = new BinaryOperator[B] { def apply(a:B, b:B) = a ++= b.result() }
    override def finisher = new java.util.function.Function[B,Map[K,V]] { def apply(s:B) = wrap(s.result()) }
    override def characteristics = java.util.Collections.emptySet() 
  }
  
  def widen[K, U, T >: U](map: Map[K,T]): Map[K,U] = map.asInstanceOf[Map[K,U]]  
}

case class Map[K,V] private (val unwrap: immutable.Map[K,V]) extends java.lang.Iterable[(K,V)] {
  import Map._
  
  def contains(key: K): Boolean = unwrap.contains(key)
  
  def filter(p: java.util.function.BiPredicate[K,V]): Map[K,V] = wrap(unwrap.filter(e => p.test(e._1,e._2)))
  
  def size:Int = unwrap.size
  
  def get(key: K): V = unwrap(key)
  
  def getOrElse[V1 >: V](key: K, fallback: java.util.function.Supplier[V1]) = unwrap.getOrElse(key, fallback.get) 
  
  def getOption(key: K): Option[V] = Option.wrap(unwrap.get(key))

  def isEmpty:Boolean = unwrap.isEmpty
  
  def plus[V1 >: V](key: K, value: V1): Map[K, V1] = wrap(unwrap + (key -> value))
  
  def plusAll[V1 >: V](other: java.lang.Iterable[(K,V1)]) = wrap (unwrap ++ other.asScala)

  def minus(key: K): Map[K, V] = wrap(unwrap - key)
  
  def minusAll(keys: java.lang.Iterable[K]): Map[K, V] = wrap(unwrap -- keys.asScala)
  
  def map[K1,V1](f: java.util.function.BiFunction[K,V,(K1,V1)]): Map[K1,V1] = wrap(unwrap.map { case (k,v) => f(k,v) })
  
  def flatMap[K1,V1](f: java.util.function.BiFunction[K,V,java.lang.Iterable[(K1,V1)]]): Map[K1,V1] =
    wrap(unwrap.flatMap { case (k,v) => f(k,v).asScala })
  
  def mapValues[V1](f: java.util.function.Function[V,V1]): Map[K,V1] = wrap(unwrap.mapValues(f(_)))
  
  def mapToSeq[T](f: java.util.function.BiFunction[K,V,T]): Seq[T] = Seq.wrap(unwrap.map { case (k,v) => f(k,v) }.toVector )
  
  def mapToSet[T](f: java.util.function.BiFunction[K,V,T]): Set[T] = Set.wrap(unwrap.map { case (k,v) => f(k,v) }.toSet )
  
  def asMap: java.util.Map[K,V] = unwrap.asJava
  
  def toHashMap: java.util.HashMap[K,V] = new java.util.HashMap(asMap)
  
  def toSeq: Seq[(K,V)] = Seq.wrap(unwrap.toVector)

  def toSet: Set[(K,V)] = Set.wrap(unwrap.toSet)
  
  def asStream: java.util.stream.Stream[(K,V)] = StreamSupport.stream(
          Spliterators.spliterator(iterator, size, Spliterator.NONNULL | Spliterator.IMMUTABLE),
          false);
  
  def keys: java.lang.Iterable[K] = unwrap.keys.asJava
  
  def keySet: Set[K] = Set.wrap(unwrap.keySet)
  
  def values: java.lang.Iterable[V] = unwrap.values.asJava
  
  def valuesToSet: Set[V] = Set.wrap(unwrap.values.toSet)
  
  def valuesToSeq: Seq[V] = Seq.wrap(unwrap.values.toVector)
  
  def foreach(f: java.util.function.BiConsumer[K,V]) = unwrap.foreach { case (k,v) => f.accept(k,v) }
  
  def exists(p: java.util.function.BiPredicate[K,V]): Boolean = unwrap.exists { case (k,v) => p.test(k,v) }
  
  def forall(p: java.util.function.BiPredicate[K,V]): Boolean = unwrap.forall { case (k,v) => p.test(k,v) }
  
  override def iterator: java.util.Iterator[(K,V)] = new java.util.Iterator[(K,V)] {
    val i = unwrap.iterator
    override def hasNext = i.hasNext
    override def next = i.next
  }  
}
