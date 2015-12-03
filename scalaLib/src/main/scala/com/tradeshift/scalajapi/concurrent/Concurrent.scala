package com.tradeshift.scalajapi.concurrent

object Concurrent {
  import scala.concurrent.{ blocking => scalaBlocking }
  
  def blocking[T](f: java.util.function.Supplier[T]): T = scalaBlocking { f.get }
  def blocking(f: java.lang.Runnable): Unit = scalaBlocking { f.run }
}
