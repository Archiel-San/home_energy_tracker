package com.archiecode.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    //Afecta todos os files que correm (possuam runtime/sao injetados) que consumam este metodo
    @Pointcut("execution(* com.archiecode.user_service.service.UserService.*(..))")
    public void serviceMethods(){}

    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint){
        log.info("Called Service Method: {}, with arguments {}", joinPoint.getSignature().getName(), joinPoint.getArgs());
    }
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfetrReturning(JoinPoint joinPoint, Object result){
        log.info(
                "Service Method: {}, returned: {}", joinPoint.getSignature().getName(), result
        );
    }


}
