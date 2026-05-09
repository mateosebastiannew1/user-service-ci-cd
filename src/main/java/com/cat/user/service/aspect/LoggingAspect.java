package com.cat.user.service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.cat.user.service.controller..*(..)) || " +
            "execution(* com.cat.user.service.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        log.debug("Entering {}.{}() with args: {}", className, methodName,
                Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("Exiting {}.{}() — took {}ms", className, methodName, elapsed);
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("Exception in {}.{}() after {}ms: {}", className, methodName, elapsed,
                    ex.getMessage());
            throw ex;
        }
    }
}
