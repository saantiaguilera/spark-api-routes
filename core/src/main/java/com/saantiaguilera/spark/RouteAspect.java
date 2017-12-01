package com.saantiaguilera.spark;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Pointcut;
import org.eclipse.jetty.util.log.Log;
import spark.route.HttpMethod;
import spark.RouteImpl;

@Aspect
public class RouteAspect {

    @Pointcut("execution(void spark.route.Routes.add(spark.route.HttpMethod,spark.RouteImpl))")
    public void add() {}

    @Around("add()")
    public Object aroundAddingARoute(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpMethod httpMethod = (HttpMethod) joinPoint.getArgs()[0];
        RouteImpl routeImpl = (RouteImpl) joinPoint.getArgs()[1];

        Log.getLog().info("Registered " + httpMethod.toString().toUpperCase() + ": " + routeImpl.getPath());

        return joinPoint.proceed();
    }

}
