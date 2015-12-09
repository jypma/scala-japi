package com.tradeshift.scalajapi.collect

object Either {
  def wrap[L,R](scalaEither: scala.util.Either[L,R]): Either[L,R] = Either(scalaEither)
  
  case class LeftProjection[L,R] private[collect] (unwrap: scala.util.Either.LeftProjection[L,R]) {
    def e: Either[L,R] = Either.wrap(unwrap.e)
    
    def exists(p: java.util.function.Predicate[_ >: L]): Boolean = unwrap.exists(p.test)
      
    def filter[Y](p: java.util.function.Predicate[_ >: L]): Option[Either[L,Y]] = 
      Option.wrap(unwrap.filter(p.test).map(Either.wrap))
      
    def flatMap[X](f: java.util.function.Function[_ >: L, Either[X, _ <: R]]): Either[X, R] =
      wrap(unwrap.flatMap(l => f.apply(l).unwrap))
    
    def forall(p: java.util.function.Predicate[_ >: L]): Boolean = unwrap.forall(p.test)
 
    def foreach(c: java.util.function.Consumer[_ >: L]): Unit = unwrap.foreach(c.accept)
    
    def get: L = unwrap.get
    
    def getOrElse(f: java.util.function.Supplier[_ <: L]): L = unwrap.getOrElse(f.get)
    
    def map[X](f: java.util.function.Function[_ >: L, X]): Either[X, R] = wrap(unwrap.map(f.apply))
    
    def toOption: Option[L] = Option.wrap(unwrap.toOption)
    
    def toSeq: Seq[L] = Seq.wrap(unwrap.toSeq.toVector)
    
    override def toString: String = unwrap.toString    
  }
  
  case class RightProjection[L,R] private[collect] (unwrap: scala.util.Either.RightProjection[L,R]) {
    def e: Either[L,R] = Either.wrap(unwrap.e)
    
    def exists(p: java.util.function.Predicate[_ >: R]): Boolean = unwrap.exists(p.test)
      
    def filter[Y](p: java.util.function.Predicate[_ >: R]): Option[Either[Y,R]] = 
      Option.wrap(unwrap.filter(p.test).map(Either.wrap))
      
    def flatMap[X](f: java.util.function.Function[_ >: R, Either[_ <: L, X]]): Either[L, X] =
      wrap(unwrap.flatMap(l => f.apply(l).unwrap))
    
    def forall(p: java.util.function.Predicate[_ >: R]): Boolean = unwrap.forall(p.test)
 
    def foreach(c: java.util.function.Consumer[_ >: R]): Unit = unwrap.foreach(c.accept)
    
    def get: R = unwrap.get
    
    def getOrElse(f: java.util.function.Supplier[_ <: R]): R = unwrap.getOrElse(f.get)
    
    def map[X](f: java.util.function.Function[_ >: R, X]): Either[L, X] = wrap(unwrap.map(f.apply))
    
    def toOption: Option[R] = Option.wrap(unwrap.toOption)
    
    def toSeq: Seq[R] = Seq.wrap(unwrap.toSeq.toVector)
    
    override def toString: String = unwrap.toString
  }    
}

object Left {
  def of[L,R](value: L): Either[L,R] = Either.wrap(scala.util.Left(value))
}

object Right {
  def of[L,R](value: R): Either[L,R] = Either.wrap(scala.util.Right(value))  
}

case class Either[L,R] private (unwrap: scala.util.Either[L,R]) {
  import Either._
  
  def isLeft: Boolean = unwrap.isLeft
  def isRight: Boolean = unwrap.isRight
  
  def left: LeftProjection[L,R] = LeftProjection(unwrap.left)
  def right: RightProjection[L,R] = RightProjection(unwrap.right)
  
  def fold[X](fa: java.util.function.Function[_ >: L,X], fb: java.util.function.Function[R,X]): X = 
    unwrap.fold(fa.apply, fb.apply) 
    
  def swap: Either[R,L] = wrap(unwrap.swap)
  
  override def toString: String = unwrap.toString
}

