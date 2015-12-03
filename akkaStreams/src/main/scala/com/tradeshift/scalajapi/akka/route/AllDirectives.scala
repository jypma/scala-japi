package com.tradeshift.scalajapi.akka.route

abstract class AllDirectives extends PathDirectives 
                                with RouteDirectives 
                                with ParameterDirectives 
                                with BasicDirectives 
                                with FutureDirectives 
                                with MarshallingDirectives 
                                with ExecutionDirectives
                                with HeaderDirectives
                                with MethodDirectives {
  
}
