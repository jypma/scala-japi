package com.tradeshift.scalajapi.test

import org.scalatest.WordSpec
import org.scalatest.WordSpecLike
import java.util.function.Consumer
import org.scalatest.FlatSpec

class ScalaTestBase extends FlatSpec {
  case class Describe(name: String, inner: Consumer[Describe]) {
    var first = true
    inner.accept(this)
    
    def should(behavior: String, spec: Runnable) {
      if (first) {
        name should behavior in {
          spec.run() 
        }
        first = false
      } else {
        it should behavior in {
          spec.run() 
        }
      }
    }
  }
  
  protected def describe(name: String, inner: Consumer[Describe]) {
    new Describe(name, inner)
  }
}
