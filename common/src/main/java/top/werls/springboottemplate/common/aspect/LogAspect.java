package top.werls.springboottemplate.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/2/8
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Pointcut("execution(public * top.werls.springboottemplate.*.controller.*.*(..))")
    public void logPointCut() {
    }

    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        log.info("before class: {} ,method: {}", joinPoint.getSignature().getDeclaringType(),joinPoint.getSignature().getName());
    }
    @After("logPointCut()")
    public void after(JoinPoint joinPoint) {
        log.info("after class: {} ,method: {}", joinPoint.getSignature().getDeclaringType(),joinPoint.getSignature().getName());
    }
    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result =joinPoint.proceed();
        log.info("after class: {} ,method: {}, time:{}ms",
                joinPoint.getSignature().getDeclaringType(),
                joinPoint.getSignature().getName()
                ,System.currentTimeMillis()-start);
        return result;
    }
}
