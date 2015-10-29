package com.tradeshift.scalajapi.concurrent

import java.util.concurrent.TimeoutException
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.time.Instant

object Await {
  @throws(classOf[TimeoutException])
  def result[T](future: Future[T], timeout: Duration): T = { 
    scala.concurrent.Await.result(future.unwrap, concurrent.duration.Duration(timeout.toMillis(), TimeUnit.MILLISECONDS))
  }

  @throws(classOf[TimeoutException])
  def result[T](future: Future[T], deadline: Instant): T = result(future, toTimeout(deadline))
  
  private def toTimeout(deadline: Instant): Duration = Duration.between(Instant.now, deadline) match {
    case timeLeft if !timeLeft.isNegative => timeLeft
    case other => Duration.ZERO
  }
  
}
