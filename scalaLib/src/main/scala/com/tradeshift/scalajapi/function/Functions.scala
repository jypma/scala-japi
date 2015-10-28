package com.tradeshift.scalajapi.function

object Functions {
  def f1[A,T](f: java.util.function.Function[A,T]): A => T = { A => f(A) }
  def f2[A,B,T](f: java.util.function.BiFunction[A,B,T]): (A,B) => T = { (A,B) => f(A,B) }
}
