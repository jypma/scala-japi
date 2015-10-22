package com.tradeshift.scalajapi.collect

import scala.collection.immutable

object Map {
  def wrap[K,V](scalaMap: immutable.Map[K,V]): Map[K,V] = new Map(scalaMap)
  
  def empty[K,V]: Map[K,V] = wrap(immutable.Map.empty)
  
  def of[K,V](k1: K, v1: V): Map[K,V] = wrap(immutable.Map(k1 -> v1))
}

class Map[K,V] private (val unwrap: immutable.Map[K,V]) {
  import Map._
  
  def plus[V1 >: V](key: K, value: V1): Map[K, V1] = wrap(unwrap + (key -> value))
  
  def mapValues[V1 >: V](f: java.util.function.Function[V,V1]): Map[K,V1] = wrap(unwrap.mapValues(f(_)))  
}
