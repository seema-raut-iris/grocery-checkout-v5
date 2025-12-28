
package com.example.grocery.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting logging for controllers and services.
 * Logs method entry/exit, execution time, and exceptions.
 */
@Aspect
@Component

public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcuts:
     *  - Controllers: com.example.grocerystore.controller..*(..)
     *  - Services:    com.example.grocerystore.service..*(..)
     */
    @Pointcut("execution(* com.example.grocery.controller..*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.example.grocery.service..*(..))")
    public void serviceLayer() {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long startNs = System.nanoTime();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getName();
        Object[] args = pjp.getArgs();

        // Entry log (arguments trimmed to avoid noisy payloads)
        //log.info("➡️  Enter {}.{}(args={})", className, methodName, safeArgs(args));

        log.info("➡️  Enter {}.{}(args={})", className, methodName, safeArgs(args));
        try {
            Object result = pjp.proceed();

            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            // Exit log (you can suppress large results if needed)
            log.info("✅ Exit {}.{} [{} ms]", className, methodName, durationMs);

            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            log.error("❌ Error in {}.{} after {} ms: {}",
                    className, methodName, durationMs, ex.toString(), ex);
            throw ex;
        }
    }

    private String safeArgs(Object[] args) {
        // Avoid printing big request bodies verbatim; truncate long strings
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String s = String.valueOf(arg);
                    return s.length() > 300 ? (s.substring(0, 300) + "...(truncated)") : s;
                })
                .toList()
                .toString();
    }
}